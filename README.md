# Apk64

## About

APK64 is a simple tool to modify APK files in java. I wrote this to use in my game engine for export projects as APK by using a pre-compiled template.

## How to use

First of all, you need an apk file to modify, but plaese note that APK64 can't apply changes to the original code of the APK. It can only change its metadata, such as the name, package, version, icon and other details.

```
...

File templateFile = ...;  // APK to modify
File outputDir = ...;     // Temporary output directory
File keyStoreFile = ...;  // KeystoreFile (can be created with FileUtils.createKeystore)
File outputFile = ...;    // Output APK file

int bufferSize = 1024; // You can increase to get faster (but don't overdo it!)
Apk64 apk64 = new Apk64(bufferSize); // Buffer size is optional

// Create and apply configurations
Apk64Configs configs = new Apk64Configs();
configs.setTemplateFile(templateFile);
configs.setOutputDir(outputDir);
configs.setKeyStoreFile(keyStoreFile, "alias", "android");
configs.setOutputFile(outputFile);
apk64.setConfigs(configs);	
		
apk64.loadTemplate(); // Start modidding APK

/* Do your changes here */

apk64.finish();       // Finish modding APK
...
```

# Standard changes

Apk64 has a few standard changes that you can do with the APK:

- Rename application
- Rename package
- Replace icon
- Replace resources (like drawables and mipmaps)
- Add and remove permissions
- Change version name and version code
- Inject assets

Bellow you can view a full example for all standard changes:

```
...
apk64.loadTemplate();

// Rename application
apk64.changeAppName("MyApp");

// Rename package
apk64.changePackage("com.your.package");

// Replace icon
apk64.replaceAppIcon(newIconFile);

// Replace resources (like drawables and mipmaps)

// With automatic extensions:
apk64.replaceDrawable("icon", newIconFile); 
apk64.replaceMipmap("icon", newIconFile); 
apk64.replaceResource("xml", "xml-file", newXMLFile);

// Specific file
apk64.replaceResource("drawable-hdpi-v4/icon.png", newIconFile); 

// Add and remove permissions
apk64.addPermission("WRITE_EXTERNAL_STORAGE");
apk64.addCustomPermission("com.package.custom.PERMISSION");

apk64.removePermission("WRITE_EXTERNAL_STORAGE");
apk64.removeCustomPermission("com.package.custom.PERMISSION");

// Change version name and version code
apk64.changeVerion(12, "1.2 mod");

// Inject assets
apk64.addToAssets(assetFile1, assetFile2, assetFile3, ... ); // You can pass multiple files and folders here

apk64.finish();
...
```

## Th'rd part libs
- [ARSCLib 1.1.3](https://github.com/REAndroid/ARSCLib/tree/9e029648845c6f9f99bdfa60a0ce2bcc51564219) to modify manifest/resources binary
- [Forwall's ApkSigner 0.7](https://github.com/fornwall/apksigner) to sign modified apk
- [Spongy Casttle 1.58.0.0](https://rtyley.github.io/spongycastle/) as dependence to forwall's ApkSigner
