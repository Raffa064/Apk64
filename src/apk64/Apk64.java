package apk64;

import com.reandroid.archive.APKArchive;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.lib.arsc.chunk.xml.ResXmlElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import net.fornwall.apksigner.CertCreator;
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
	
	public void setConfigs(Apk64Configs configs) throws Exception {
		System.out.println("[ Applying configs ]");

		setTemplate(configs.templateFile);
		setOutputDir(configs.outputDir);
		setKeystore(configs.keyStoreFile, configs.keyAlias, configs.keyPassword);
		setOutputFile(configs.outputFile);
	}

	public Apk64 setTemplate(File templateFile) {
		System.out.println("[ Using template file: '" + templateFile + "' ]");

		this.templateFile = templateFile;
		
		return this;
	}
	
	public Apk64 setOutputDir(File outputDir) throws Exception {
		System.out.println("[ Using output directory: '" + outputDir + "' ]");
		
		this.outputDir = outputDir;
		this.assetsDir = new File(outputDir, "assets");
		this.resourceDir = new File(outputDir, "res");
		this.metaInfDir = new File(outputDir, "META-INF");
		this.manifestFile = new File(outputDir, "AndroidManifest.xml");
		this.resourcesFile = new File(outputDir, "resources.arsc");
		this.compressedFile = new File(outputDir.getParent(), "compressed.zip");
		
		return this;
	}
		
	public Apk64 setKeystore(File keyStoreFile, String alias, String password) throws Exception {
		keyAlias = alias;
		keyPassword = password;
		
		loadKeyStore(keyStoreFile);
		
		return this;
	}
	
	public Apk64 setOutputFile(File outputFile) {
		System.out.println("[ Output file: '" + outputFile + "' ]");

		this.outputFile = outputFile;
		
		return this;
	}

	public void loadTemplate() throws Exception {
		System.out.println("[ Loading template: " + templateFile + " ]");

		createOutputDir();
		extractApk();
		removeMetaInf();
		loadManifest();
		loadResourcesARSC();
		
		System.out.println("[ APK is ready to changes ]");
	}

	public void finish() throws Exception {
		System.out.println("[ Finishing... ]");
		
		writeManifest();
		writeResources();
		compress();
		sign();
		deleteTrash();
		
		System.out.println("[ Finished ]");
	}

	private void loadKeyStore(File keyStoreFile) throws Exception {
		System.out.println("[ Loading KeyStore ]");
	
		if (!keyStoreFile.exists()) {
			System.out.println("[ Invalid keyStore: Using default]");

			keyAlias = "alias";
			keyPassword = "android";
			
			System.out.println("Creating default keystore (using '" + keyPassword + "' as password and '" + keyAlias + "' as the key alias).");
            
			FileUtils.createKeyStore(keyStoreFile, keyAlias, keyPassword, "APK64", "Earth", "Earth");
        }

		keyStore = KeyStoreFileManager.loadKeyStore(keyStoreFile.getAbsolutePath(), null);
	}
	
	private void createOutputDir() {
		System.out.println("[ Creating output dir: '" + outputDir + "' ]");
		
		if (outputDir.exists()) {
			FileUtils.deleteFiles(outputDir);
		}
		
		outputDir.mkdir();
	}

	private void extractApk() throws Exception {
		System.out.println("[ Extracting apk ]");

		APKArchive archive = APKArchive.loadZippedApk(templateFile);

		archive.extract(outputDir);
	}

	private void removeMetaInf() throws Exception {
		System.out.println("[ Removing META-INF ]");

		if (!FileUtils.deleteFiles(metaInfDir)) {
			throw new Exception("Error on delete META-INF");
		}
	}

	private void loadManifest() throws Exception {
		System.out.println("[ Loading manifest ]");
		
        manifest = AndroidManifestBlock.load(manifestFile);

        System.out.println("Package name: " + manifest.getPackageName());

        List<String> permissions = manifest.getUsesPermissions();
		
        for (String permission : permissions){
			System.out.println("Uses permission: " + permission);
        }
	}

	private void loadResourcesARSC() throws Exception {
		System.out.println("[ Loading resources.arsc ]");
		
        resources = TableBlock.load(resourcesFile);

        Collection<PackageBlock> packageBlockList = resources.listPackages();
		
        System.out.println("Packages count = "+packageBlockList.size());
        for (PackageBlock packageBlock : packageBlockList){
			System.out.println("Package id = " + packageBlock.getId() + ", name = " + packageBlock.getName());
        }
	}

	private void writeManifest() throws Exception {
		System.out.println("[ Writing manifest ]");
		
		System.out.println(manifest.toString());
		manifest.refresh();
        manifest.writeBytes(manifestFile);
	}

	private void writeResources() throws IOException {
		System.out.println("[ Writing resources ]");
		
		resources.refresh();
        resources.writeBytes(resourcesFile);	
	}

	private void compress() throws Exception {
		System.out.println("[ Compressing modified apk ]");
		
		ZipUtils.zip(outputDir.listFiles(), compressedFile);
	}

	private void sign() throws KeyStoreException, GeneralSecurityException, IOException {
		System.out.println("[ Signing apk ]");
		
        X509Certificate publicKey = (X509Certificate) keyStore.getCertificate(keyAlias);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
        
		ZipSigner.signZip(publicKey, privateKey, "SHA1withRSA", compressedFile.getAbsolutePath(), outputFile.getAbsolutePath());
	}

	private void deleteTrash() {
		System.out.println("[ Deleting trash ]");
		FileUtils.deleteFiles(outputDir);
		FileUtils.deleteFiles(compressedFile);
	}
	
	public void changePackage(String pkg) {
		System.out.println("[ Changing package to '" + pkg + "' ]");
		manifest.setPackageName(pkg);
	}
	
	public void changeAppName(String name) {
		System.out.println("[ Changing name to '" + name + "' ]");
		
		// Change on manifest
		ResXmlAttribute label = manifest.getApplicationElement().searchAttributeByResourceId(AndroidManifestBlock.ID_label);
		label.setValueAsString(name);
		
		for (ResXmlElement activity : manifest.listActivities()) {
			label = activity.searchAttributeByResourceId(AndroidManifestBlock.ID_label);
			label.setValueAsString(name);
		}
    }
	
	public void changeAppIcon(int id) {
		System.out.println("[ Changing icon to '" + id + "' ]");

		// Change on manifest
		ResXmlAttribute label = manifest.getApplicationElement().searchAttributeByResourceId(AndroidManifestBlock.ID_icon);
		label.setValueAsResourceId(id);

		for (ResXmlElement activity : manifest.listActivities()) {
			label = activity.searchAttributeByResourceId(AndroidManifestBlock.ID_icon);
			label.setValueAsResourceId(id);
		}
    }
	
	public void addPermission(String permission) {
		addCustomPermission("android.permission." + permission);
	}
	
	public void addCustomPermission(String permission) {
		System.out.println("[ Adding permission: " + permission + " ]");
		
		manifest.addUsesPermission(permission);
	}
	
	public void removePermission(String permission) {
		removeCustomPermission("android.permission." + permission);
	}
	
	public void removeCustomPermission(String permission) {
		System.out.println("[ Removing permission: " + permission + " ]");

		ResXmlElement permissionElement = manifest.getUsesPermission(permission);
		manifest.getManifestElement().removeElement(permissionElement);
	}
	
	public void changeVersion(int code, String name) {
		System.out.println("[ Changing version to '" + name + "' (" + code + ") ]");
		
	    manifest.setVersionCode(code);
        manifest.setVersionName(name);
	}
	
	public void replaceResource(String source, File targetFile) {
		File sourceFile = new File(resourceDir, source);
		
		if (sourceFile.exists()) {
			System.out.println("[ Replacing '" + source + "' to '" + targetFile + "' ]");
			
			FileUtils.deleteFiles(sourceFile);
		    FileUtils.copyFiles(targetFile, sourceFile);
		}
	}
	
	public void replaceDrawable(String source, File targetFile) {
		String[] versions = {"", "-v4", "-v7"};
		String[] types = {"", "-ldpi", "-mdpi", "-hdpi", "-xhdpi", "-xxhdpi", "-xxxhdpi"};
		
		for (String version : versions) {
			for (String type : types) {
				replaceResource("drawable" + type + version + "/" + source, targetFile);
			}
		}
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
		System.out.println("[ Changing activity orientation: '" + activityNameWithPackage + "' ]");
		
		ResXmlElement activity = getActivity(activityNameWithPackage);
		
		System.out.println(activity);
		
		ResXmlAttribute attr =  activity.getOrCreateAndroidAttribute("screenOrientation", AndroidManifestBlock.ID_screenOrientation);
		attr.setValueAsInteger(orientation);
	}
	
	public File getAssets() {
		assetsDir.mkdir();
		
		return assetsDir;
	}
}
