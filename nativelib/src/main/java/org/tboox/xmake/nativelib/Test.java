package org.tboox.xmake.nativelib;

import android.util.Log;

public class Test {

    private static final String TAG = "xmake-nativelib";

    public static void loadTests() {
        System.loadLibrary("c++_shared");
        System.loadLibrary("nativelib");
        Log.e(TAG, getNativeInfo());
    }
    public static native String getNativeInfo();
}
