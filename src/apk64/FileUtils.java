package apk64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import net.fornwall.apksigner.CertCreator;

public class FileUtils {
	public static byte[] readFile(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[fis.available()];
		fis.read(buffer);

		fis.close();
		
		return buffer;
	}
	
	public static String readFileString(File file) throws Exception {
		byte[] buffer = readFile(file);
		String content = new String(buffer);
		return content;
	}
	
	public static void writeFile(File file, byte[] buffer) throws Exception {
		FileOutputStream fis = new FileOutputStream(file);
		
		fis.write(buffer);
		
		fis.flush();
		fis.close();
	}
	
	public static void writeFile(File file, String content) throws Exception {
		byte[] buffer = content.getBytes();
		writeFile(file, buffer);
	}

	public static boolean deleteFiles(File file) {
		System.out.println("Deleting: "+file);

		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				if (!deleteFiles(f)) {
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

	public static void createKeyStore(File keyStoreFile, String keyAlias, String keyPassword, String commonName, String organization, String organizationUnit) {
		System.out.println("Creating new keystore (using '" + keyPassword + "' as password and '" + keyAlias + "' as the key alias).");

		CertCreator.DistinguishedNameValues nameValues = new CertCreator.DistinguishedNameValues();

		nameValues.setCommonName(commonName);
		nameValues.setOrganization(organization);
		nameValues.setOrganizationalUnit(organizationUnit);

		CertCreator.createKeystoreAndKey(keyStoreFile.getAbsolutePath(), keyPassword.toCharArray(), "RSA", 2048, keyAlias, keyPassword.toCharArray(), "SHA1withRSA", 30, nameValues);
	}
}
