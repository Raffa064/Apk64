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
		
		File templateFile = new File(test, "template.apk");
		File outputDir = new File(test, "output");
		File keyStoreFile = new File(test, "keystore");
		File outputFile = new File(test, "output.apk");
		
		Apk64 apk64 = new Apk64();
		
		Apk64Configs configs = new Apk64Configs();
		configs.setTemplateFile(templateFile);
		configs.setOutputDir(outputDir);
		configs.setKeyStoreFile(keyStoreFile, "alias", "android");
		configs.setOutputFile(outputFile);
		
		apk64.setConfigs(configs);	
		apk64.loadTemplate();
		
		applyChanges(test, apk64);
		
		apk64.finish();
	}

	private static void applyChanges(String test, Apk64 apk64) {
		File project = new File(test, "project");
		File icon = new File(test, "newIcon.jpg");
		
		// Change metadatas
		apk64.changePackage("com.example.game");
		apk64.changeVersion(2, "1.1 Sokoban");
		apk64.changeAppName("Sokoban");
		apk64.changeActivityOrientation("com.raffa064.engine.MainActivity", Apk64.SCREEN_ORIENTATION_LANDSCAPE);
		
		// Change permissions
		apk64.addPermission("WRITE_EXTERNAL_STORAGE");
		
		// Inject project into assets
		apk64.addToAssets(project);

		// Change icon
		apk64.replaceDrawable("ic_launcher.jpg", icon);
		apk64.replaceDrawable("ic_launcher.png", icon);
	}

	

}
