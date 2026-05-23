package ma.aybi.chroconi.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import kotlin.text.Charsets;

public class PreferencesManager {
    private static final String PREF_NAME = "chroconi_pref";

    private static final String FIRST_TIME = "first_time";
    private static final String SALT = "salt";
    private static final String TOKEN = "tkn";

    private static SharedPreferences getPrefs(
            Context context) {
        return context
                .getApplicationContext()
                .getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE);
    }

    // CHECK FIRST TIME
    public static boolean isFirstTime(Context context) {

        return getPrefs(context).getBoolean(
                FIRST_TIME,
                true);
    }
    // SAVE FIRST TIME
    public static void setFirstTime(Context context, boolean value) {
        getPrefs(context).edit()
                .putBoolean(FIRST_TIME, value)
                .apply();
    }
    public static byte[] getToken(Context context) {
        String slt = getPrefs(context).getString(TOKEN, null);
        if (slt != null) {
            return slt.getBytes(Charsets.ISO_8859_1);
        }
        return null;
    }
    public static void setToken(Context context, byte[] token) {
        getPrefs(context).edit()
                .putString(TOKEN, new String(token, Charsets.ISO_8859_1))
                .apply();
    }

    public static byte[] getSalt(Context context) {
        String slt = getPrefs(context).getString(SALT, null);
        if (slt != null) {
            return slt.getBytes(Charsets.ISO_8859_1);
        }
        return null;
    }
    public static void setSalt(Context context, byte[] salt) {
        getPrefs(context).edit()
                .putString(SALT, new String(salt, Charsets.ISO_8859_1))
                .apply();
    }
}
