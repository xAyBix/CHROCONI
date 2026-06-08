package ma.aybi.chroconi.security;

import android.util.Base64;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ma.aybi.chroconi.util.Constants;

public final class MessageCrypto {

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String RSA_CIPHER = "RSA/ECB/PKCS1Padding";
    private static final String AES_CIPHER = "AES/GCM/NoPadding";

    public static byte[] encrypt(byte[] plaintext, PublicKey publicKey) {
        try {
            SecretKey sessionKey = generateAESKey();
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher aesCipher = Cipher.getInstance(AES_CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, spec);
            byte[] ciphertext = aesCipher.doFinal(plaintext);

            Cipher rsaCipher = Cipher.getInstance(RSA_CIPHER);
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedKey = rsaCipher.doFinal(sessionKey.getEncoded());

            byte[] result = new byte[encryptedKey.length + iv.length + ciphertext.length];
            System.arraycopy(encryptedKey, 0, result, 0, encryptedKey.length);
            System.arraycopy(iv, 0, result, encryptedKey.length, iv.length);
            System.arraycopy(ciphertext, 0, result, encryptedKey.length + iv.length, ciphertext.length);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] decrypt(byte[] data, PrivateKey privateKey) {
        try {
            int keySizeBytes = Constants.RSA_KEY_SIZE / 8;
            byte[] encryptedKey = new byte[keySizeBytes];
            System.arraycopy(data, 0, encryptedKey, 0, keySizeBytes);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(data, keySizeBytes, iv, 0, GCM_IV_LENGTH);

            byte[] ciphertext = new byte[data.length - keySizeBytes - GCM_IV_LENGTH];
            System.arraycopy(data, keySizeBytes + GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher rsaCipher = Cipher.getInstance(RSA_CIPHER);
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKeyBytes = rsaCipher.doFinal(encryptedKey);
            SecretKeySpec sessionKey = new SecretKeySpec(aesKeyBytes, "AES");

            Cipher aesCipher = Cipher.getInstance(AES_CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            aesCipher.init(Cipher.DECRYPT_MODE, sessionKey, spec);
            return aesCipher.doFinal(ciphertext);
        } catch (Exception e) {
            return null;
        }
    }

    private static SecretKey generateAESKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(AES_KEY_SIZE);
        return kg.generateKey();
    }
}
