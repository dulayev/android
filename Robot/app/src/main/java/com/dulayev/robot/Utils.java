package com.dulayev.robot;

import android.util.Log;

import java.util.Map;
import java.util.Set;

/**
 * Created by dulayev on 19.05.18.
 */

public class Utils {

    public static String TAG = "silence";

    static String formatStringSet(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        sb.append('|');
        for(String s : set) {
            sb.append(s);
            sb.append('|');
        }
        return sb.toString();
    }

    static void Log(String msg) {
        Log.d(TAG, msg);
    }
    static void Log(String name, Object object) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(": ");
        sb.append(object);
        Log(sb.toString());
    }
}
