package ma.aybi.chroconi.security;

import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import kotlin.text.Charsets;
import ma.aybi.chroconi.model.Message;
import ma.aybi.chroconi.util.Constants;

public final class Encryptor {
    private static SecretKeySpec secretKey;
    private static byte[] salt;

    private Encryptor () {}
    public static Message decryptMessage (byte[] encryptedMessage) {
        return Message.deserialize(
          decrypt(encryptedMessage)
        );
    }
    public static byte[] decrypt (byte[] encryptedData) {
        // Extract IV (first 12 bytes)
        byte[] iv = new byte[Constants.GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, Constants.GCM_IV_LENGTH);

        // Extract ciphertext (remaining bytes)
        byte[] ciphertext = new byte[encryptedData.length - Constants.GCM_IV_LENGTH];
        System.arraycopy(encryptedData, Constants.GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        try {
            // Decrypt
            Cipher cipher = Cipher.getInstance(Constants.AES_ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(Constants.GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] objectBytes = cipher.doFinal(ciphertext);


            return objectBytes;
        } catch (Exception e) {
            return null;
        }
    }
    public static byte[] encryptMessage (Message message) {
        byte[] serializedMessage = message.serialize();
        return encrypt(serializedMessage);
    }
    public static byte[] encrypt (byte[] data) {
        // Generate random IV
        byte[] iv = new byte[Constants.GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        try {
            // Encrypt
            Cipher cipher = Cipher.getInstance(Constants.AES_ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(Constants.GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(data);

            // Combine: [IV] + [Ciphertext]
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

            return result;
        }catch (Exception e) {
            return null;
        }
    }
    public static void generateKey (String password) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt,
                Constants.KEY_ITERATIONS, Constants.KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(Constants.KEY_ALGO);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            secretKey = new SecretKeySpec(keyBytes, "AES");
        }catch (Exception e) {

        }


    }

    public static byte[] generateSalt () {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    public static byte[] getSalt () {
        return salt;
    }
    public static void setSalt (byte[] value) {
        salt = value;
    }
    public static String byteToString (byte [] data) {
        return new String(data, Charsets.ISO_8859_1);
    }
    public static byte[] stringToByte (String text) {
        return text.getBytes(Charsets.ISO_8859_1);
    }
}
