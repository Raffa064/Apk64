package apk64;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileNotFoundException;
import java.util.zip.ZipEntry;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;

public class ZipUtils {
	public static void zip(File[] files, File zipFile, int bufferSize) throws Exception {
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		
		for (File file : files) {
			addFileToZip("", file, zos, bufferSize);
		}
		
		zos.flush();
		zos.close();
		
		fos.flush();
		fos.close();
	}
	
	private static void addFileToZip(String path, File file, ZipOutputStream zos, int bufferSize) throws IOException {
		if (file.isFile()) {
			String entryName = path + file.getName();
			System.out.println("\tCompressing: "+entryName);
			
			ZipEntry entry = new ZipEntry(entryName);
			zos.putNextEntry(entry);

			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			
			byte[] buffer = new byte[bufferSize];
			int length = 0;
			
			while ((length = bis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}

			bis.close();
			fis.close();
			
			return;
		}
		
		for (File f : file.listFiles()) {
			addFileToZip(path + file.getName() + "/", f, zos, bufferSize);
		}
	}
	
}
