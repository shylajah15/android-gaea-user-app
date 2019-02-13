package com.example.apple.gaeauserapp;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {


    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    //    shared pref mode
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "gaea-user-app";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_FIRST_TIME_SOCIAL_MEDIA_LAUNCH = "IsFirstTimeSocialMediaLaunch";

    public PrefManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime){
        editor.putBoolean(IS_FIRST_TIME_LAUNCH,isFirstTime);
        editor.commit();
    }
    public void setFirstTimeSocialMediaLaunch(boolean isFirstTime){
        editor.putBoolean(IS_FIRST_TIME_SOCIAL_MEDIA_LAUNCH,isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch(){
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH,true);
    }
    public boolean isFirstTimeSocialMediaLaunch(){
        return pref.getBoolean(IS_FIRST_TIME_SOCIAL_MEDIA_LAUNCH,true);
    }
}
