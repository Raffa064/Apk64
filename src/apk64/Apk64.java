package apk64;

import apk64.Logger.Profile;
import com.reandroid.archive.APKArchive;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.lib.arsc.chunk.xml.ResXmlElement;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.value.EntryBlock;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import net.fornwall.apksigner.KeyStoreFileManager;
import net.fornwall.apksigner.ZipSigner;

public class Apk64 {
	public static final int SCREEN_ORIENTATION_BEHIND = 3;
    public static final int SCREEN_ORIENTATION_FULL_SENSOR = 10;
    public static final int SCREEN_ORIENTATION_FULL_USER = 13;
    public static final int SCREEN_ORIENTATION_LANDSCAPE = 0;
    public static final int SCREEN_ORIENTATION_LOCKED = 14;
    public static final int SCREEN_ORIENTATION_NOSENSOR = 5;
    public static final int SCREEN_ORIENTATION_PORTRAIT = 1;
    public static final int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
    public static final int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;
    public static final int SCREEN_ORIENTATION_SENSOR = 4;
    public static final int SCREEN_ORIENTATION_SENSOR_LANDSCAPE = 6;
    public static final int SCREEN_ORIENTATION_SENSOR_PORTRAIT = 7;
    public static final int SCREEN_ORIENTATION_UNSPECIFIED = -1;
    public static final int SCREEN_ORIENTATION_USER = 2;
    public static final int SCREEN_ORIENTATION_USER_LANDSCAPE = 11;
    public static final int SCREEN_ORIENTATION_USER_PORTRAIT = 12;
	
	public static final String[] RESOURCE_VERSIONS = {"", "-v4", "-v7"};
	public static final String[] RESOURCE_RESOLUTIONS = {"", "-ldpi", "-mdpi", "-hdpi", "-xhdpi", "-xxhdpi", "-xxxhdpi"};
	
	private int bufferSize = 1024;
	private File templateFile;
	private File outputDir;
	private File assetsDir;
	private File resourceDir;
	private File metaInfDir;
	private File manifestFile;
	private File resourcesFile;
	private File compressedFile;
	private File outputFile;
	private KeyStore keyStore;
	private String keyAlias;
	private String keyPassword;
	private AndroidManifestBlock manifest;
	private TableBlock resources;
	private Logger logger = new Logger();
	
	public Apk64() {}

	public Apk64(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	public void setLogProfile(Profile profile) {
		logger.setProfile(profile);
	}
	
	public void setConfigs(Apk64Configs configs) {
		logger.D("[ Applying configs ]");

		setTemplate(configs.templateFile);
		setOutputDir(configs.outputDir);
		setKeystore(configs.keyStoreFile, configs.keyAlias, configs.keyPassword);
		setOutputFile(configs.outputFile);
	}

	public Apk64 setTemplate(File templateFile) {
		logger.D("[ Using template file: '" + templateFile + "' ]");

		this.templateFile = templateFile;
		
		return this;
	}
	
	public Apk64 setOutputDir(File outputDir) {
		logger.D("[ Using output directory: '" + outputDir + "' ]");
		
		this.outputDir = outputDir;
		this.assetsDir = new File(outputDir, "assets");
		this.resourceDir = new File(outputDir, "res");
		this.metaInfDir = new File(outputDir, "META-INF");
		this.manifestFile = new File(outputDir, "AndroidManifest.xml");
		this.resourcesFile = new File(outputDir, "resources.arsc");
		this.compressedFile = new File(outputDir.getParent(), "compressed.zip");
		
		return this;
	}
		
	public Apk64 setKeystore(File keyStoreFile, String alias, String password) {
		keyAlias = alias;
		keyPassword = password;
		
		loadKeyStore(keyStoreFile);
		
		return this;
	}
	
	public Apk64 setOutputFile(File outputFile) {
		logger.D("[ Output file: '" + outputFile + "' ]");

		this.outputFile = outputFile;
		
		return this;
	}

	public void loadTemplate() {
		logger.D("[ Loading template: " + templateFile + " ]");
		
		if (!templateFile.exists()) {
			throw new Error(logger, "Error on load template file");
		}

		createOutputDir();
		extractApk();
		removeMetaInf();
		loadManifest();
		loadResourcesARSC();
		
		logger.W("[ APK is ready to changes ]");
		
		Chrono.start(templateFile.getName());
	}

	public void finish() {
		logger.D("[ Finishing... ]");
		
		writeManifest();
		writeResources();
		compress();
		sign();
		deleteTrash();

		long time = Chrono.end(templateFile.getName());		
		logger.D("[ Finished in " + (time / 1000000000) + "s ]");
	}

	private void loadKeyStore(File keyStoreFile) {
		logger.D("[ Loading KeyStore ]");
	
		if (!keyStoreFile.exists()) {
			logger.D("[ Invalid keyStore: Using default]");

			keyAlias = "alias";
			keyPassword = "android";
			
			logger.W("Creating default keystore (using '" + keyPassword + "' as password and '" + keyAlias + "' as the key alias).");
            
			FileUtils.createKeyStore(keyStoreFile, keyAlias, keyPassword, "APK64", "Earth", "Earth");
        }

		try {
			keyStore = KeyStoreFileManager.loadKeyStore(keyStoreFile.getAbsolutePath(), null);
		} catch (Exception e) {
			throw new Error(logger, "Error on load keystore file: " + e.getMessage());
		}
	}
	
	private void createOutputDir() {
		logger.D("[ Creating output dir: '" + outputDir + "' ]");
		
		if (outputDir.exists()) {
			FileUtils.deleteFiles(outputDir);
		}
		
		outputDir.mkdir();
	}

	private void extractApk() {
		logger.D("[ Extracting apk ]");

		try {
			APKArchive archive = APKArchive.loadZippedApk(templateFile);
			archive.extract(outputDir);
		} catch (IOException e) {
			throw new Error(logger, "Error on extract apk: " + e.getMessage());
		}
	}

	private void removeMetaInf() {
		logger.D("[ Removing META-INF ]");

		if (!FileUtils.deleteFiles(metaInfDir)) {
			throw new Error(logger, "Error on delete META-INF");
		}
	}

	private void loadManifest() {
		logger.D("[ Loading manifest ]");
		
        try {
			manifest = AndroidManifestBlock.load(manifestFile);
			logger.D("Package name: " + manifest.getPackageName());

			List<String> permissions = manifest.getUsesPermissions();
			for (String permission : permissions) {
				logger.D("Uses permission: " + permission);
			}
		} catch (IOException e) {
			throw new Error(logger, "Error on load manifest.xml: " + e.getMessage());
		}
	}

	private void loadResourcesARSC() {
		logger.D("[ Loading resources.arsc ]");
		
        try {
			resources = TableBlock.load(resourcesFile);

			Collection<PackageBlock> packageBlockList = resources.listPackages();
			logger.D("-> Packages count: " + packageBlockList.size());

			for (PackageBlock packageBlock : packageBlockList) {
				logger.D("\tPackage: " + packageBlock.getName());
			}
		} catch (IOException e) {
			throw new Error(logger, "Error on load resources.arsc: " + e.getMessage());
		}
	}

	private void writeManifest() {
		logger.D("[ Writing manifest ]");
		
		try {
			logger.D(manifest.toString());
			manifest.refresh();
			manifest.writeBytes(manifestFile);
		} catch (IOException e) {
			throw new Error(logger, "Error on write manifest: " + e.getMessage());
		}
	}

	private void writeResources() {
		logger.D("[ Writing resources ]");
		
		try {
			resources.refresh();
			resources.writeBytes(resourcesFile);
		} catch (IOException e) {
			throw new Error(logger, "Error on write resources.arsc: " + e.getMessage());
		}	
	}

	private void compress() {
		logger.D("[ Compressing modified apk ]");
		
		try {
			ZipUtils.zip(outputDir.listFiles(), compressedFile, bufferSize);
		} catch (Exception e) {
			throw new Error(logger, "Error on compress files: " + e.getMessage());
		}
	}

	private void sign() {
		logger.D("[ Signing apk ]");
		
        try {
			X509Certificate publicKey = (X509Certificate) keyStore.getCertificate(keyAlias);
			PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());

			ZipSigner.signZip(publicKey, privateKey, "SHA1withRSA", compressedFile.getAbsolutePath(), outputFile.getAbsolutePath());
		} catch (Exception e) {
			throw new Error(logger, "Error on sign apk: " + e.getMessage());
		}
	}

	private void deleteTrash() {
		logger.D("[ Deleting trash ]");
		
		FileUtils.deleteFiles(outputDir);
		FileUtils.deleteFiles(compressedFile);
	}
	
	public void changePackage(String pkg) {
		logger.D("[ Changing package to '" + pkg + "' ]");
		manifest.setPackageName(pkg);
	}
	
	public void changeAppName(String name) {
		logger.D("[ Changing name to '" + name + "' ]");
		
		// Change on manifest
		ResXmlElement application = manifest.getApplicationElement();
		ResXmlAttribute label = application.searchAttributeByResourceId(AndroidManifestBlock.ID_label);
		
		if (label == null) {
			label = new ResXmlAttribute();
			label.setNameResourceID(AndroidManifestBlock.ID_label);
			application.addAttribute(label);
		} 
			
		label.setValueAsString(name);
		
		for (ResXmlElement activity : manifest.listActivities()) {
			label = activity.searchAttributeByResourceId(AndroidManifestBlock.ID_label);
			if (label == null) {
				label = new ResXmlAttribute();
				label.setNameResourceID(AndroidManifestBlock.ID_label);
				activity.addAttribute(label);
			}
			
			label.setValueAsString(name);	
		}
    }
	
	public void changeAppIcon(int resId) {
		logger.D("[ Changing icon to '" + resId + "' ]");

		// Change on manifest
		ResXmlAttribute label = manifest.getApplicationElement().searchAttributeByResourceId(AndroidManifestBlock.ID_icon);
		label.setValueAsResourceId(resId);

		for (ResXmlElement activity : manifest.listActivities()) {
			label = activity.searchAttributeByResourceId(AndroidManifestBlock.ID_icon);
			label.setValueAsResourceId(resId);
		}
    }
	
	public void replaceAppIcon(File newIconFile) {
		logger.D("[ Replacing app icon ]");
		logger.W("Warning: replaceAppIcon() may not work if the apk has round icon");

		logger.D("-> Replacing by resource names...");
		for (PackageBlock pkg : resources.listPackages()) {
			EntryGroup iconGroup = pkg.getEntryGroup(manifest.getIconResourceId());
			logger.D("\t"+pkg.getName()+": "+iconGroup.getSpecName());
			
			for (EntryBlock entry : iconGroup.getItems()) {
				File oldIconFile = new File(outputDir, entry.getValueAsString());
				
				String parentName = oldIconFile.getParentFile().getName();
				String name = oldIconFile.getName();
				
				logger.D("\t" + parentName + "/" + name);
				
				FileUtils.deleteFiles(oldIconFile);
				
				if (FileUtils.isImage(oldIconFile) == FileUtils.isImage(newIconFile)) {
					FileUtils.copyFiles(newIconFile, oldIconFile);
		        }
			}
		}
	}
	
	public void addPermission(String permission) {
		addCustomPermission("android.permission." + permission);
	}
	
	public void addCustomPermission(String permission) {
		logger.D("[ Adding permission: " + permission + " ]");
		
		manifest.addUsesPermission(permission);
	}
	
	public void removePermission(String permission) {
		removeCustomPermission("android.permission." + permission);
	}
	
	public void removeCustomPermission(String permission) {
		logger.D("[ Removing permission: " + permission + " ]");

		ResXmlElement permissionElement = manifest.getUsesPermission(permission);
		manifest.getManifestElement().removeElement(permissionElement);
	}
	
	public void changeVersion(int code, String name) {
		logger.D("[ Changing version to '" + name + "' (" + code + ") ]");
		
	    manifest.setVersionCode(code);
        manifest.setVersionName(name);
	}
	
	public void replaceResource(String source, File targetFile) {
		File sourceFile = new File(resourceDir, source);
		
		if (sourceFile.exists()) {
			logger.D("[ Replacing '" + source + "' to '" + targetFile + "' ]");
			
			FileUtils.deleteFiles(sourceFile);
		    FileUtils.copyFiles(targetFile, sourceFile);
		}
	}
	
	public void replaceResource(String dir, String source, File targetFile) {
		for (String version : RESOURCE_VERSIONS) {
			for (String resolution : RESOURCE_RESOLUTIONS) {
				for (String extension : FileUtils.IMAGE_EXTENSIONS) {
					File sourceFile = new File(resourceDir+"/"+dir+version+resolution, source+extension);
					
					if (sourceFile.exists()) {
						logger.D("[ Replacing '" + source + "' to '" + targetFile + "' ]");

						FileUtils.deleteFiles(sourceFile);
						FileUtils.copyFiles(targetFile, sourceFile);
					}
				}
			}
		}
	}

	public void replaceDrawable(String source, File targetFile) {
		replaceResource("drawable", source, targetFile);
	}
	
	public void replaceMipmap(String source, File targetFile) {
		replaceResource("mipmap", source, targetFile);
	}
	
	public void addToAssets(File... assetFiles) {
		for (File file : assetFiles) {
			File inAssets = new File(assetsDir, file.getName());
			FileUtils.copyFiles(file, inAssets);
		}
	}
	
	public ResXmlElement getActivity(String activityNameWithPackage) {
		for (ResXmlElement activity : manifest.listActivities()) {
			String name = activity.searchAttributeByResourceId(AndroidManifestBlock.ID_name).getValueAsString();
			
			if (name.equals(activityNameWithPackage)) {
				return activity;
			}
		}
		
		return null;
	}
	
	public void changeActivityOrientation(String activityNameWithPackage, int orientation) {
		logger.D("[ Changing activity orientation: '" + activityNameWithPackage + "' ]");
		
		ResXmlElement activity = getActivity(activityNameWithPackage);		
		ResXmlAttribute attr =  activity.getOrCreateAndroidAttribute("screenOrientation", AndroidManifestBlock.ID_screenOrientation);
		attr.setValueAsInteger(orientation);
	}
	
	public File getAssets() {
		assetsDir.mkdir();
		
		return assetsDir;
	}
	
	public static class Error extends java.lang.Error {
		private Logger logger;

		public Error(Logger logger, String msg) {
			super(msg);
			this.logger = logger;
		}
	}
}
