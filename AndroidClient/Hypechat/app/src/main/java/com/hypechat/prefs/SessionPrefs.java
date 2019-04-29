package com.hypechat.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.hypechat.models.LoginBody;
import com.hypechat.models.User;

public class SessionPrefs {
    private static SessionPrefs INSTANCE;

    public static final String PREFS_NAME = "HYPECHAT_PREFS";
    public static final String PREF_USERNAME = "PREF_USERNAME";
    //public static final String PREF_USER_TOKEN = "PREF_USER_TOKEN";

    private final SharedPreferences mPrefs;
    private boolean mIsLoggedIn = false;

    public static SessionPrefs get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionPrefs(context);
        }
        return INSTANCE;
    }

    private SessionPrefs(Context context) {
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        //mIsLoggedIn = !TextUtils.isEmpty(mPrefs.getString(PREF_USER_TOKEN, null));
    }

    public boolean isLoggedIn(){
        return mIsLoggedIn;
    }

    public void saveUser(LoginBody user) {
        if (user != null) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(PREF_USERNAME, user.getUsername());
            //editor.putString(PREF_USER_TOKEN, user.getToken());
            editor.apply();

            mIsLoggedIn = true;
        }
    }

    public void logOut(){
        mIsLoggedIn = false;
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_USERNAME, null);
        //editor.putString(PREF_USER_TOKEN, null);
        editor.apply();
    }
}
