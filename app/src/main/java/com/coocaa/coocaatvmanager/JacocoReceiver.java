package com.coocaa.coocaatvmanager;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JacocoReceiver extends BroadcastReceiver {
    static String ACTION_JACOCO_START = "com.coocaa.os.JacocoReceiver.start";
    static String ACTION_JACOCO_STOP = "com.coocaa.os.JacocoReceiver.stop";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("JacocoReceiver", "act = " + intent.getAction());
        if (TextUtils.equals(intent.getAction(), ACTION_JACOCO_START)) {
            Log.e("JacocoReceiver", "start");
            JacocoUtils.start(context,context.getApplicationContext());
        } else if (TextUtils.equals(intent.getAction(), ACTION_JACOCO_STOP)) {
            Log.e("JacocoReceiver", "stop");
            JacocoUtils.stop(context.getApplicationContext());
        }
    }


}

class JacocoUtils {

    private static Map<Object, File> runners = new LinkedHashMap<>();

    public static void start(Context context, Object object) {
        if (object == null) {
            return;
        }
        if (runners.containsKey(object)) {
            return;
        }
        File dirFile = new File("/data/local/coverage");
        dirFile.mkdirs();
        File file = create(new File("/data/local/"));//Environment.getExternalStorageDirectory());
//        file.setReadable(True);
        Log.d("JacocoUtils", "start file:" + file.getAbsolutePath());
        if (file == null) {
            file = create(context.getFilesDir());
        }
        if (file != null && file.exists()) {
            runners.put(object, file);
        }
    }

    private static File create(File root) {
        if (root == null) {
            return null;
        }
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Log.e(TAG, "setPosixFilePermission");
                Files.setPosixFilePermissions(Paths.get(root.getAbsolutePath()), perms);
            } catch (IOException e) {
                Log.e(TAG, "setPosixFilePermissions writeDataToCSV: " + e);
            }
        }
        File file = new File(root, "coverage.ec");
        file.setReadable(true);
        file.setWritable(true);
        file.setExecutable(true);
        if (file.exists()) {
            file.delete();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
//                Log.d("JacocoUtils", "新建文件异常：" + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void stop(Object object) {
        File file = runners.get(object);
        if (file != null) {
            generateCoverageReport(file);
        }
        runners.remove(object);
    }


    private static void generateCoverageReport(File file) {
        Log.d(TAG, "generateCoverageReport():" + file);
        OutputStream out = null;
        try {
            out = new FileOutputStream(file, false);
            Object agent = Class.forName("org.jacoco.agent.rt.RT")
                    .getMethod("getAgent")
                    .invoke(null);
            byte[] data = (byte[]) agent.getClass().getMethod("getExecutionData", boolean.class)
                    .invoke(agent, false);
            out.write(data);
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}