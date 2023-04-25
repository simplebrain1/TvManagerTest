package com.coocaa.coocaatvmanager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import com.coocaa.alarmslibrary.Alarms;
import com.coocaa.coocaatvmanager.utils.StorageUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
//import com.netease.qa.emmagee.utils.FpsInfo;

//import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected static String TAG = MainActivity.class.getSimpleName();
    Button btn_storage_info,btn_app_storage_info
            ,btn_clear_cache,btn_clear_no_used_apps
            ,btn_check_process,btn_big_file_scann,btn_excel,btn_frame,btn_stop;
    TextView storage_info,edit_define_no_user_time_tip;
    EditText edit_define_no_user_time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        JacocoUtils.start(this,getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_storage_info = findViewById(R.id.btn_storage_info);
        btn_app_storage_info = findViewById(R.id.btn_app_storage_info);
        btn_clear_cache = findViewById(R.id.btn_clear_cache);
        btn_clear_no_used_apps = findViewById(R.id.btn_clear_no_used_apps);
        edit_define_no_user_time = findViewById(R.id.edit_define_no_user_time);
        edit_define_no_user_time_tip=findViewById(R.id.edit_define_no_user_time_tip);
        btn_check_process = findViewById(R.id.btn_check_process);
        btn_big_file_scann = findViewById(R.id.btn_big_file_scann);
        btn_excel = findViewById(R.id.btn_excel);
        btn_frame  = findViewById(R.id.btn_frame);
        btn_stop = findViewById(R.id.btn_stop);

        storage_info = findViewById(R.id.storage_info);
        btn_storage_info.setOnClickListener(this);
        btn_app_storage_info.setOnClickListener(this);
        btn_clear_cache.setOnClickListener(this);
        btn_clear_no_used_apps.setOnClickListener(this);
        btn_check_process.setOnClickListener(this);
        btn_big_file_scann.setOnClickListener(this);
        btn_excel.setOnClickListener(this);
        btn_frame.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

//        test();
//        test1();
//        setShowView();
//        StorageUtil.getInstance().queryAppStorageTotalSize(this);
    }
    public void setShowView(){
        edit_define_no_user_time.setVisibility(View.GONE);
        edit_define_no_user_time_tip.setVisibility(View.GONE);
        storage_info.setVisibility(View.GONE);
        btn_storage_info.setVisibility(View.GONE);
        btn_app_storage_info.setVisibility(View.GONE);
        btn_clear_cache.setVisibility(View.GONE);
        btn_clear_no_used_apps.setVisibility(View.GONE);
        btn_check_process.setVisibility(View.GONE);
        btn_big_file_scann.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG , "test-----isHardwareAccelerated();"+ btn_stop.isHardwareAccelerated());
//        handler.postDelayed(task,1000);
    }

    @Override
    protected void onDestroy() {
        JacocoUtils.stop(getApplicationContext());
        super.onDestroy();
    }

    public void testFps(){
        float fps = FpsInfo.fps();
        Log.i(TAG," testFps:"+fps);
    }
    private Handler handler = new Handler();
    private Runnable task = new Runnable() {

        public void run() {
				testFps();
				handler.postDelayed(this, 1000);
        }
    };

    public void test(){
        Log.i(TAG , "test");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void test1(){
        Log.i(TAG , "test1");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

////    private  File create(File root) {
////        if (root == null) {
////            return null;
////        }
////        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            perms.add(PosixFilePermission.OWNER_READ);
//            perms.add(PosixFilePermission.OWNER_WRITE);
//            perms.add(PosixFilePermission.OWNER_EXECUTE);
//            perms.add(PosixFilePermission.GROUP_READ);
//            perms.add(PosixFilePermission.GROUP_WRITE);
//            perms.add(PosixFilePermission.GROUP_EXECUTE);
//            perms.add(PosixFilePermission.OTHERS_READ);
//            perms.add(PosixFilePermission.OTHERS_WRITE);
//            perms.add(PosixFilePermission.OTHERS_EXECUTE);
//        }
//        boolean hasDir = root.exists();
//        if (!hasDir) {
//            root.mkdirs();// 这里创建的是目录
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                try {
//                    Files.setPosixFilePermissions(Paths.get(root.getAbsolutePath()), perms);
//                } catch (IOException e) {
//                    Log.e("JacocoUtils", "setPosixFilePermissions writeDataToCSV: " + e);
//                }
//            } else {
//                ShellUtils.CommandResult command = ShellUtils.execCommand("chmod 777 " + root.getAbsolutePath(), false);
//                Log.d("JacocoUtils", "writeDataToCSV: CommandResult:" + command);
//            }
//        }
//        File file = new File(root, "coverage.ec");
//        if (file.exists()) {
//            file.delete();
//        }
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    try {
//                        Files.setPosixFilePermissions(Paths.get(file.getAbsolutePath()), perms);
//                    } catch (IOException e) {
//                        Log.e("JacocoUtils", "setPosixFilePermissions writeDataToCSV: " + e);
//                    }
//                } else {
//                    ShellUtils.CommandResult command = ShellUtils.execCommand("chmod 774 " + file.getAbsolutePath(), false);
//                    Log.d("JacocoUtils", "writeDataToCSV: CommandResult:" + command);
//                }
//                return file;
//            } catch (IOException e) {
//                Log.d("JacocoUtils", "新建文件异常：" + file.getAbsolutePath());
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//    static String path = Environment.getExternalStorageDirectory();
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_storage_info:

//                File file = create(new File("/data/local/tmp/"));
//                CoocaaStorageUtil.StorageEntity storageEntity = CoocaaStorageUtil.getInstance().queryWithStorageManager(this);
                StorageUtil.StorageEntity storageEntity
                        = StorageUtil.getInstance().queryWithStorageManager(this);//queryWithStatFs(this);
                storage_info.setText(storageEntity.toString());
                break;
            case R.id.btn_app_storage_info:
                startActivity(new Intent(this, CoocaaAppManagerActivity.class));
                break;
            case R.id.btn_clear_cache:
                startActivity(new Intent(this, CoocaaClearCacheActivity.class));
                break;
            case R.id.btn_clear_no_used_apps:
                int day = 1;
                String inputDay = edit_define_no_user_time.getText().toString();
                if(!TextUtils.isEmpty(inputDay)){
                    if(TextUtils.isDigitsOnly(inputDay)){
                        day = Integer.parseInt(inputDay);
                    }else{
                        Toast.makeText(this,"请输入纯数字天数",Toast.LENGTH_SHORT).show();
                        return ;
                    }
                }
                Log.i(TAG," day:"+day);
//                ArrayList<UsageStats> appUsageStatsWithIntervalTime = CoocaaAppSizeUtils.getInstance().getAppUsageStatsWithIntervalTime(this, day*24 * 60 * 60 * 1000);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        for (UsageStats usageStats :appUsageStatsWithIntervalTime){
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                Log.i(TAG," usageStats:"+usageStats.getPackageName()+"---"+ DateFormat.format("M月d日",usageStats.getLastTimeUsed()));
//                                CoocaaAppSizeUtils.getInstance().unInstallApp(MainActivity.this,usageStats.getPackageName(), 1);
//                            }
//                        }
//                    }
//                }).start();
                Intent intent = new Intent(this, CoocaaRecentNoUserAppManagerActivity.class);
                intent.putExtra("overtime",day);
                startActivity(intent);
                break;
            case R.id.btn_check_process:
                startActivity(new Intent(this, CoocaaTaskManagerActivity.class));
                break;
            case R.id.btn_big_file_scann:
//                new FileScannThread(Environment.getExternalStorageDirectory(), new FileScannThread.OnScanFileThreadListener() {
//                    @Override
//                    public void foundFle(File f) {
//                        Log.i(TAG," foundFle:"+f.getName()+"---"+f.getPath());
//                    }
//
//                    @Override
//                    public void foundBigFile(File f) {
//                        Log.i(TAG," foundBigFile:"+f.getName()+"---"+f.getPath());
//                    }
//
//                    @Override
//                    public void completed() {
//                        Log.i(TAG," completed:");
//                    }
//                }).start();
//                startActivity(new Intent(this, CoocaaFileScannerActivity.class));
                break;
            case R.id.btn_excel:
                // 找到一个文件
//                List<Student> list = new ArrayList<>();
//                list.add(new Student().setId(1).setScore(11).setName("喜洋洋"));
//                list.add(new Student().setId(2).setScore(22).setName("美羊羊"));
//                list.add(new Student().setId(3).setScore(33).setName("懒洋洋"));
//                LinkedHashMap<String, String> fieldMap = new LinkedHashMap<>();
//                fieldMap.put("id","ID");
//                fieldMap.put("name","名称");
//                fieldMap.put("score","分数");
//                File file = new File(getFilesDir(),"test.xls");
//                Log.i(TAG ," file.getpath :"+file.getAbsolutePath());
//                FileOutputStream outputStream = null;
//                try {
//                    outputStream = new FileOutputStream(file);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    ExcelUtils.listToExcel(list,fieldMap,"花名册",outputStream);
//                } catch (ExcelException e) {
//                    e.printStackTrace();
//                }
                Log.i(TAG," onclick:");
//                Intent sub_intent = new Intent();
//                sub_intent.setAction("com.coocaa.os.theme.action.THEME_SETTINGS");
//                Intent explicitIntent = getExplicitIntent(this, sub_intent);
//                sub_intent.setPackage("com.coocaa.os.thememanager");   //指定应用包名
//                intent.addCategory(Intent.CATEGORY_DEFAULT);
//                startActivity(sub_intent);

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    Settings.Global.putInt(getContentResolver(),
//                            "enable_gpu_debug_layers",
//                            1);
//                }
                //定时任务
//                Alarms.setNextAlert(this);
//                System.exit(0);//杀进程
                break;
            case R.id.btn_frame:
                //重复间隔任务
//                Alarms.setNextAlert(this, SystemClock.elapsedRealtime()+5000,120*1000);
//                System.exit(0);
                break;
            case R.id.btn_stop:
                //取消任务
//                Alarms.disableAlert(this);
//                break;
        }
    }

}