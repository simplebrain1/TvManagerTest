package com.coocaa.coocaajacocolibrary;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JacocoUtils {

    private static String DEFAULT_COVERAGE_FILE_PATH = "/sdcard";

    private static int TYPE_DEFAULT_COVERAGE_FILE_PATH = 0;

    public static boolean setCoverageFilePath(String filePath){
        if(filePath != null && filePath.length() > 0) {
            DEFAULT_COVERAGE_FILE_PATH = filePath;
            return true;
        }
        return false;
    }

    public static boolean setCoverageFilePathType(int pathType){
        if(pathType != 0) {
            TYPE_DEFAULT_COVERAGE_FILE_PATH = pathType;
            return true;
        }
        return false;
    }

    public static void start(Context context) {
        if (TYPE_DEFAULT_COVERAGE_FILE_PATH==0){
            DEFAULT_COVERAGE_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }else if (TYPE_DEFAULT_COVERAGE_FILE_PATH==1){
            DEFAULT_COVERAGE_FILE_PATH = context.getFilesDir().getAbsolutePath();
        }else if (TYPE_DEFAULT_COVERAGE_FILE_PATH==2){
//            DEFAULT_COVERAGE_FILE_PATH = "/data/local/tmp"; //需要system权限执行shell命令创建
        }
        File root = new File(DEFAULT_COVERAGE_FILE_PATH);
        create(root);
        Log.d("JacocoUtils", "file type:"+TYPE_DEFAULT_COVERAGE_FILE_PATH+",start file:" + DEFAULT_COVERAGE_FILE_PATH);
    }

    private static boolean create(File root) {
        if (root == null) {
            return false;
        }
        File file = new File(root, "coverage.ec");
        if (file.exists()) {
            if (TYPE_DEFAULT_COVERAGE_FILE_PATH==2){
                ShellUtils.CommandResult result = ShellUtils.execCommand("rm "+DEFAULT_COVERAGE_FILE_PATH+"/coverage.ec",false);
                Log.d("JacocoUtils","delete result:"+result);
            }else{
                file.delete();
            }
        }
        if (!file.exists()) {
            try {

                if (TYPE_DEFAULT_COVERAGE_FILE_PATH==2) {
                    ShellUtils.CommandResult result = ShellUtils.execCommand("touch "+DEFAULT_COVERAGE_FILE_PATH+"/coverage.ec", false);
                    Log.d("JacocoUtils", "result:" + result);
                    result = ShellUtils.execCommand("chmod 777 "+DEFAULT_COVERAGE_FILE_PATH+"/coverage.ec", false);
                    Log.d("JacocoUtils", "result1:" + result);
                }else{
                    file.createNewFile();
                }
                return true;
            } catch (IOException e) {
                Log.d("JacocoUtils", "新建文件异常：" + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void stop() {
        File file = new File(DEFAULT_COVERAGE_FILE_PATH, "coverage.ec");
        if (file.exists()) {
            Log.d("JacocoUtils","stop");
            generateCoverageReport(file);
        }
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