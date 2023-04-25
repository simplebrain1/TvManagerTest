package com.coocaa.coocaatvmanager;

import android.app.Application;

//import com.argusapm.android.api.ApmTask;
//import com.argusapm.android.api.Client;
//import com.argusapm.android.core.Config;
//import com.argusapm.android.core.Manager;
//import com.argusapm.android.core.tasks.ITask;
//import com.argusapm.android.core.tasks.TaskManager;
//import com.argusapm.android.utils.ProcessUtils;
//import com.netease.qa.emmagee.utils.Settings;


public class TvApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        JacocoUtils.start(this,getApplicationContext());
//        initMontors();
//        initAppConfig();
    }

    private void initAppConfig() {
        // create directory of emmagee
//        File dir = new File(Settings.EMMAGEE_RESULT_DIR);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
    }

//    private void initMontors() {
//        boolean isUi = TextUtils.equals(getPackageName(), ProcessUtils.getCurrentProcessName());
//        Config.ConfigBuilder builder = new Config.ConfigBuilder()
//                .setAppContext(this)
////                .setRuleRequest(new RuleSyncRequest())
////                .setUpload(new CollectDataSyncUpload())
//                .setAppName("apm_demo")
//                .setAppVersion("1.0.0")
//                .setApmid("apm_demo");//该ID是在APM的后台进行申请的
//
//        Log.i("TvApp"," isUi:"+isUi);
//        //单进程应用可忽略builder.setDisabled相关配置。
//        if (!isUi) { //除了“主进程”，其他进程不需要进行数据上报、清理等逻辑。“主进程”通常为常驻进行，如果无常驻进程，即为UI进程。
//            builder.setDisabled(ApmTask.FLAG_DATA_CLEAN)
//                    .setDisabled(ApmTask.FLAG_CLOUD_UPDATE)
//                    .setDisabled(ApmTask.FLAG_DATA_UPLOAD)
//                    .setDisabled(ApmTask.FLAG_COLLECT_ANR)
//                    .setDisabled(ApmTask.FLAG_COLLECT_FILE_INFO);
//        }
//        builder.setEnabled(ApmTask.FLAG_COLLECT_ACTIVITY_AOP); //activity采用aop方案时打开，默认关闭即可。
////        builder.setEnabled(ApmTask.FLAG_LOCAL_DEBUG); //是否读取本地配置，默认关闭即可。
//        Client.attach(builder.build());
//        Client.isDebugOpen(true);//设置成true的时候将会打开悬浮窗
////        List<ITask> allTask = TaskManager.getInstance().getAllTask();
////        for (ITask iTask:allTask) {
////            Manager.getInstance().getTaskManager().updateTaskSwitchByTaskName(iTask.getTaskName(), true);
////        }
//        Client.startWork();
//    }

//    Window.OnFrameMetricsAvailableListener listener;
//    public void startFrameMetrics(View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            final String activityName = getClass().getSimpleName();
//            listener = new Window.OnFrameMetricsAvailableListener() {
//                private int allFrames = 0;
//                private int jankyFrames = 0;
//
//                @Override
//                public void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int dropCountSinceLastInvocation) {
//                    FrameMetrics frameMetricsCopy = new FrameMetrics(frameMetrics);
//                    allFrames++;
//                    float totalDurationMs = (float) (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.TOTAL_DURATION));
//                    if (totalDurationMs > 17) {
//                        jankyFrames++;
//                        String msg = String.format("Janky frame detected on %s with total duration: %.2fms\n", activityName, totalDurationMs);
//                        float layoutMeasureDurationMs = (float) (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION));
//                        float drawDurationMs = (float) (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.DRAW_DURATION));
//                        float gpuCommandMs = (float) (0.000001 * frameMetricsCopy.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION));
//                        float othersMs = totalDurationMs - layoutMeasureDurationMs - drawDurationMs - gpuCommandMs;
//                        float jankyPercent = (float) jankyFrames / allFrames * 100;
//                        msg += String.format("Layout/measure: %.2fms, draw:%.2fms, gpuCommand:%.2fms others:%.2fms\n", layoutMeasureDurationMs, drawDurationMs, gpuCommandMs, othersMs);
//                        msg += "Janky frames: " + jankyFrames + "/" + allFrames + "(" + jankyPercent + "%)" + dropCountSinceLastInvocation;
//                        Log.e("FrameMetrics", msg);
//                    }
//                }
//            };
//            getWindow().addOnFrameMetricsAvailableListener(listener, new Handler());
//        } else {
//            Log.w("FrameMetrics", "FrameMetrics can work only with Android SDK 24 (Nougat) and higher");
//        }
//    }
}
