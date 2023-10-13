package apk64;

import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import net.fornwall.apksigner.CertCreator;
import net.fornwall.apksigner.KeyStoreFileManager;
import net.fornwall.apksigner.ZipSigner;

public class APKSignerTest {
    public static void ma_in(String... args) throws Exception {
		String tests = "/storage/emulated/0/AppProjects/Signer/src/net/fornwall/apksigner/MyOwn/tests/";
        String keystorePath = tests + "keystore";
        String inputFile = tests + "input.apk";
        String outputFile = tests + "output.apk";

        char[] keyPassword = "android".toCharArray();

        File keystoreFile = new File(keystorePath);
        if (!keystoreFile.exists()) {
            String alias = "alias";
            System.out.println("Creating new keystore (using 'android' as password and '" + alias + "' as the key alias).");
            CertCreator.DistinguishedNameValues nameValues = new CertCreator.DistinguishedNameValues();
            nameValues.setCommonName("APK Signer");
            nameValues.setOrganization("Earth");
            nameValues.setOrganizationalUnit("Earth");
            CertCreator.createKeystoreAndKey(keystorePath, keyPassword, "RSA", 2048, alias, keyPassword, "SHA1withRSA", 30, nameValues);
        }

        KeyStore keyStore = KeyStoreFileManager.loadKeyStore(keystorePath, null);
        String alias = keyStore.aliases().nextElement();

        X509Certificate publicKey = (X509Certificate) keyStore.getCertificate(alias);
        try {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword);
            ZipSigner.signZip(publicKey, privateKey, "SHA1withRSA", inputFile, outputFile);
        } catch (UnrecoverableKeyException e) {
            System.err.println("apksigner: Invalid key password.");
            System.exit(1);
        }
    }
}
