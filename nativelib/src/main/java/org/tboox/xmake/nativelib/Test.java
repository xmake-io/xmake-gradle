package org.tboox.xmake.nativelib;

public class Test {

    public static String loadTests() {
        System.loadLibrary("c++_shared");
        System.loadLibrary("nativelib");
        return getNativeInfo();
    }
    public static native String getNativeInfo();
}
