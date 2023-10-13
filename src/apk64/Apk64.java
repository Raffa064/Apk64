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
	private File outputDir;
	private File templateFile;
	private File compressedFile;
	private File outputFile;
	private File manifestFile;
	private File resourcesFile;
	private KeyStore keyStore;
	private String keyAlias;
	private String keyPassword;
	private AndroidManifestBlock manifest;
	private TableBlock resources;

	public void config(File outputDir, File templateFile) throws Exception {
		System.out.println("[ Applying Configs ]");

		this.outputDir = outputDir;
		this.templateFile = templateFile;
		this.compressedFile = new File(outputDir.getParent(), "compressed.zip");
		this.outputFile = new File(outputDir.getParent(), "output.apk");
		this.manifestFile = new File(outputDir, "AndroidManifest.xml");
		this.resourcesFile = new File(outputDir, "resources.arsc");
	}
	
	public void setKeystore(File keyStoreFile, String alias, String password) throws Exception {
		keyAlias = alias;
		keyPassword = password;
		
		loadKeyStore(keyStoreFile);
	}

	public void loadTemplate() throws Exception {
		System.out.println("[ Loading template: " + templateFile + " ]");

		extractApk();
		removeMetaInf();
		loadManifest();
		loadResourcesARSC();
	}

	public void finish() throws Exception {
		writeManifest();
		writeResources();
		compress();
		sign();
		deleteOutputDir();
	}

	private void loadKeyStore(File keyStoreFile) throws Exception {
		System.out.println("[ Loading KeyStore ]");
		
		String keystorePath = keyStoreFile.getAbsolutePath();

		if (!keyStoreFile.exists()) {
			System.out.println("[ Invalid keyStore: Using default]");

			keyAlias = "alias";
			keyPassword = "android";
			
			System.out.println("Creating new keystore (using '" + keyPassword + "' as password and '" + keyAlias + "' as the key alias).");
            
			CertCreator.DistinguishedNameValues nameValues = new CertCreator.DistinguishedNameValues();
            nameValues.setCommonName("APK Signer");
            nameValues.setOrganization("Earth");
            nameValues.setOrganizationalUnit("Earth");
            CertCreator.createKeystoreAndKey(keystorePath, keyPassword.toCharArray(), "RSA", 2048, keyAlias, keyPassword.toCharArray(), "SHA1withRSA", 30, nameValues);
        }

		keyStore = KeyStoreFileManager.loadKeyStore(keystorePath, null);
	}

	private void extractApk() throws Exception {
		System.out.println("[ Extracting apk ]");

		APKArchive archive = APKArchive.loadZippedApk(templateFile);

		archive.extract(outputDir);
	}

	private void removeMetaInf() throws Exception {
		System.out.println("[ Removing META-INF ]");

		File metaInfDir = new File(outputDir, "META-INF");

		if (!deleteFile(metaInfDir)) {
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
	
	private void deleteOutputDir() {
		System.out.println("[ Deleting output directory ]");
//		deleteFile(outputDir);
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
	
	public void changeVersion(int code, String name) {
		System.out.println("[ Changing version to '" + name + "' (" + code + ") ]");
		
	    manifest.setVersionCode(code);
        manifest.setVersionName(name);
	}
	
	public void replaceResource(String source, File targetFile) {
		File sourceFile = new File(outputDir, "res/"+source);
		
		if (sourceFile.exists()) {
			System.out.println("[ Replacing '" + source + "' to '" + targetFile + "' ]");
			
			deleteFile(sourceFile);
		    copyFiles(targetFile, sourceFile);
		}
	}
	
	public void replaceDrawable(String source, File targetFile) {
		String[] versions = {"", "-v4", "-v7"};
		String[] types = {"", "-mdpi", "-hdpi", "-xhdpi", "-xxhdpi", "-xxxhdpi"};
		
		for (String version : versions) {
			for (String type : types) {
				replaceResource("drawable" + type + version + "/" + source, targetFile);
			}
		}
	}
	
	public File getAssets() {
		File assetsDir = new File(outputDir, "assets");
		assetsDir.mkdir();
		
		return assetsDir;
	}
	
	private boolean deleteFile(File file) {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Are you sure to delete \""+file+"\"? (Y/any)");
//		if (!sc.next().equals("Y")) {
//			return false;
//		}
		
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				if (!deleteFile(f)) {
					return false;
				}
			}
		}
		
		file.delete();
		
		return true;
	}
	
	public static void copyFiles(File originFile, File targetFile) {
		final Path originPath = originFile.toPath();
		final Path targetPath = targetFile.toPath();

		try {
			Files.walkFileTree(originPath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Path targetFile = targetPath.resolve(originPath.relativize(file));
						Files.copy(file, targetFile);

						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						Path targetDir = targetPath.resolve(originPath.relativize(dir));
						Files.createDirectories(targetDir);

						return FileVisitResult.CONTINUE;
					}
				});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
