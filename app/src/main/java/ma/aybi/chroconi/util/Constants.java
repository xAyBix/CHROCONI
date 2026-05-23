package ma.aybi.chroconi.util;

public class Constants {
    // KEY
    public static final String KEY_ALGO = "PBKDF2WithHmacSHA256";
    public static final int KEY_ITERATIONS = 100_000;
    public static final int KEY_LENGTH = 256;
    
    // AES
    public static final String AES_ALGO = "AES/GCM/NoPadding";
    public static final int GCM_TAG_LENGTH = 128;
    public static final int GCM_IV_LENGTH = 12;

}
