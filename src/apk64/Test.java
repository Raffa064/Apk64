package apk64;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class Test {
	public static void main(String... args) throws Exception {
		String test = "/storage/emulated/0/AppProjects/Signer/tests/";
		File outputDir = new File(test, "output");
		outputDir.mkdir();
		File templateFile = new File(test, "template.apk");
		File project = new File(test, "project");
		File icon = new File(test, "newIcon.jpg");
		File keyStoreFile = new File(test, "keystore");

		Apk64 apk64 = new Apk64();

		apk64.config(outputDir, templateFile);
		apk64.setKeystore(keyStoreFile, "alias", "android");
		apk64.loadTemplate();
		
		// Apply changes

		apk64.changePackage("com.example.game");
		apk64.changeAppName("Sokoban");

		File assets = apk64.getAssets();
		
		File assets_project = new File(assets, "project");

		apk64.copyFiles(project, assets_project);
		apk64.replaceDrawable("ic_launcher.jpg", icon);
		apk64.replaceDrawable("ic_launcher.png", icon);
		
		// Generate modified apk

		apk64.finish();
	}

	

}
