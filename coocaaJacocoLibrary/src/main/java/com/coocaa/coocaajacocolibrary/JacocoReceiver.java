package com.coocaa.coocaajacocolibrary;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;



public class JacocoReceiver extends BroadcastReceiver {
    static String ACTION_JACOCO_START = "com.coocaa.os.JacocoReceiver.start";
    static String ACTION_JACOCO_STOP = "com.coocaa.os.JacocoReceiver.stop";
    static String ACTION_SET_JACOCO_PATH = "com.coocaa.os.JacocoReceiver.setPath";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("JacocoReceiver", "act = " + intent.getAction());
        if (TextUtils.equals(intent.getAction(), ACTION_JACOCO_START)) {
            Log.d("JacocoReceiver", "start");
            JacocoUtils.start(context);
        } else if (TextUtils.equals(intent.getAction(), ACTION_JACOCO_STOP)) {
            Log.d("JacocoReceiver", "stop");
            JacocoUtils.stop();
        } else if (TextUtils.equals(intent.getAction(), ACTION_SET_JACOCO_PATH)) {
            String coverage_path = intent.getStringExtra("path");
            int coverage_path_type = intent.getIntExtra("pathType",0);
            Log.d("JacocoReceiver", "setPath:"+coverage_path);
            Log.d("JacocoReceiver", "setPath coverage_path_type:"+coverage_path_type);
            boolean ret = JacocoUtils.setCoverageFilePath(coverage_path);
            boolean retType = JacocoUtils.setCoverageFilePathType(coverage_path_type);
            Log.d("JacocoReceiver", "setPath ret:"+ret+" ,retType:"+retType);
        }
    }


}

