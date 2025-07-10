package edu.northeastern.suyt.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String USER_SESSION_PREF = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences sharedPref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPref = context.getSharedPreferences(USER_SESSION_PREF, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void saveLoginSession() {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
