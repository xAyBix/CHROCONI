package ma.aybi.chroconi.security;

import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import ma.aybi.chroconi.util.Constants;

public final class KeyUtils {

    private KeyUtils() {}

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(Constants.RSA_ALGO);
            generator.initialize(Constants.RSA_KEY_SIZE);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String publicKeyToString(PublicKey key) {
        return Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP);
    }

    public static String privateKeyToString(PrivateKey key) {
        return Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP);
    }

    public static PublicKey stringToPublicKey(String encoded) {
        try {
            byte[] keyBytes = Base64.decode(encoded, Base64.NO_WRAP);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance(Constants.RSA_ALGO);
            return factory.generatePublic(spec);
        } catch (Exception e) {
            return null;
        }
    }

    public static PrivateKey stringToPrivateKey(String encoded) {
        try {
            byte[] keyBytes = Base64.decode(encoded, Base64.NO_WRAP);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance(Constants.RSA_ALGO);
            return factory.generatePrivate(spec);
        } catch (Exception e) {
            return null;
        }
    }

    public static PrivateKey decryptStoredPrivateKey(String privateKeyEncrypted) {
        byte[] encryptedBytes = Encryptor.stringToByte(privateKeyEncrypted);
        byte[] rawBytes = Encryptor.decrypt(encryptedBytes);
        if (rawBytes == null) return null;
        return stringToPrivateKey(Base64.encodeToString(rawBytes, Base64.NO_WRAP));
    }
}
