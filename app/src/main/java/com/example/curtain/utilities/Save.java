package com.example.curtain.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class Save {


//    SharedPreferences ga yozish va o'qish uchun klass

//    Void saving
    public void save(Context ctx, String name, String value){
        SharedPreferences s = ctx.getSharedPreferences("Curtain", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = s.edit();
        editor.putString(name, value);
        editor.apply();
    }

//    Get Value Return String
    public static String read(Context ctx, String main, String name, String defaultValue){
        SharedPreferences s = ctx.getSharedPreferences(main, Context.MODE_PRIVATE);
        return s.getString(name, defaultValue);
    }

//    Boshqa activitydan chaqirib ishlatish

//    Save.save(getApplicationContext(), "name", "value")
//    Save.read(getApplicationContext(), "Main", "name", "defaultValue")

}
