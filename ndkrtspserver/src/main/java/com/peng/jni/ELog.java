package com.peng.jni;

import android.util.Log;

public class ELog {
    public static void e(String tag, String msg){
        Log.e(tag,msg);
    }

    public static void i(String tag, String msg){
        Log.i(tag,msg);
    }
}
