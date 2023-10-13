package apk64;

import java.io.File;

public class Apk64Configs {
	public File templateFile;
	public File outputDir;
	public File keyStoreFile;
	public String keyAlias;
	public String keyPassword;
	public File outputFile;

	public void setTemplateFile(File templateFile) {
		this.templateFile = templateFile;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public void setKeyStoreFile(File keyStoreFile, String keyAlias, String keyPassword) {
		this.keyStoreFile = keyStoreFile;
		this.keyAlias = keyAlias;
		this.keyPassword = keyPassword;
	}
	
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
}
