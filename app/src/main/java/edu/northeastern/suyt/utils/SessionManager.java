package edu.northeastern.suyt.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;

import edu.northeastern.suyt.model.User;


public class SessionManager {
    private static final String USER_SESSION_PREF = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_RECYCLE_POINTS = "recycle_points";
    private static final String KEY_REDUCE_POINTS = "reduce_points";
    private static final String KEY_REUSE_POINTS = "reuse_points";
    private static final String KEY_USER_RANK = "user_rank";
    private static final String KEY_SAVED_POSTS = "saved_posts";

    private static final String KEY_QUOTE = "saved_quote";
    private static final String KEY_TIMESTAMP = "quote_timestamp";

    private final SharedPreferences sharedPref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPref = context.getSharedPreferences(USER_SESSION_PREF, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void setSavedQuote(String quote) {
        editor.putString(KEY_QUOTE, quote);
        editor.apply();
    }

    public void setQuoteTimestamp(long timestamp) {
        editor.putLong(KEY_TIMESTAMP, timestamp);
        editor.apply();
    }

    public void saveUserData(User user) {
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putInt(KEY_RECYCLE_POINTS, user.getUserStats().getRecyclePoints());
        editor.putInt(KEY_REDUCE_POINTS, user.getUserStats().getReducePoints());
        editor.putInt(KEY_REUSE_POINTS, user.getUserStats().getReusePoints());
        editor.putStringSet(KEY_SAVED_POSTS, new HashSet<>(user.getSavedPosts()));
        editor.apply();
    }

    public void saveLoginSession() {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return sharedPref.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        return sharedPref.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return sharedPref.getString(KEY_EMAIL, null);
    }

    public int getRecyclePoints() {
        return sharedPref.getInt(KEY_RECYCLE_POINTS, 0);
    }

    public int getReducePoints() {
        return sharedPref.getInt(KEY_REDUCE_POINTS, 0);
    }

    public int getReusePoints() {
        return sharedPref.getInt(KEY_REUSE_POINTS, 0);
    }

    public String getUserRank() {
        return sharedPref.getString(KEY_USER_RANK, null);
    }

    public ArrayList<String> getSavedPosts() {
        return new ArrayList<>(sharedPref.getStringSet(KEY_SAVED_POSTS, new HashSet<>()));
    }

    public String getSavedQuote() {
        return sharedPref.getString(KEY_QUOTE, null);
    }

    public long getQuoteTimestamp() {
        return sharedPref.getLong(KEY_TIMESTAMP, 0);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
