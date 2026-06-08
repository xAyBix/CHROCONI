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

    // RSA
    public static final String RSA_ALGO = "RSA";
    public static final int RSA_KEY_SIZE = 2048;

    // PREFIXES
    public static final String REPO_PREFIXES = "CHROCONI_CONV_";
    public static final String CONVERSATIONS_FILE = "conversations.enc";

}
