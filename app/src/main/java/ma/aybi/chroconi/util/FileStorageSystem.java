package ma.aybi.chroconi.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import ma.aybi.chroconi.security.Encryptor;

public class FileStorageSystem {

    public static void saveEncrypted(Context context, byte[] data) {
        try {
            File file = new File(context.getFilesDir(), Constants.CONVERSATIONS_FILE);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
        }
    }

    public static byte[] loadEncrypted(Context context) {
        try {
            File file = new File(context.getFilesDir(), Constants.CONVERSATIONS_FILE);
            if (!file.exists()) return null;
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return data;
        } catch (Exception e) {
            return null;
        }
    }
}
