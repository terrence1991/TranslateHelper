package com.zy.translate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Yong on 2016/3/31.
 */
public class SPHelper {

    private static SharedPreferences sharedPreferences;

    public static void init(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean hasStarted(){
        return sharedPreferences.getBoolean("has_started", false);
    }

    public static void setStarted(boolean hasStarted){
        sharedPreferences.edit()
                .putBoolean("has_started", hasStarted)
                .apply();
    }
}
