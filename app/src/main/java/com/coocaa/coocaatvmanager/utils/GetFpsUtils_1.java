package com.coocaa.coocaatvmanager.utils;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class GetFpsUtils_1 {

    //清空之前采样的数据，防止统计重复的时间
    private static String clearCommand = "dumpsys SurfaceFlinger --latency-clear";//""adb shell dumpsys SurfaceFlinger --latency-clear";
    private static int jumpingFrames = 0; //jank次数，跳帧数
    private static int totalFrames = 0;  //统计的总帧数
    private static double lostFrameRate = 0; //丢帧率
    private static double fps; //fps值
    public static long fpsVsncTime = 16670000; //16.67ms(单位纳秒)
    public static long betweenFrameExceptionInterval = 500000000; //500ms(单位纳秒)两帧异常时间

    private static double[] info;

    private static int stuckSum, stuckSum2;

    private static float lastSS;

    public static int getStuckSum() {
        return stuckSum;
    }

    public static int getStuckSum2() {
        return stuckSum2;
    }

    public static void clearFrameData(String packageName) {
        try {
            clearCommand = "dumpsys gfxinfo " + packageName + " reset";
            Runtime.getRuntime().exec(clearCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearFrameData2(String packageName) {
        try {
            clearCommand = "dumpsys SurfaceFlinger --latency-clear";//"dumpsys gfxinfo " + packageName + " reset";
            Runtime.getRuntime().exec(clearCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static double[] getMegeInfo(String deviceName, String packageName){
//        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
//           return getInfo(null,packageName);
//        }else{
//            String clsName = SysUtils.getSystemProperty("sky.current.actname");
//            return getInfo2(packageName,clsName);
//        }
//    }

    public static double[] getInfo(String deviceName, String packageName) {
        long start = System.currentTimeMillis();
        Log.i("GetFpsUtils", " start :" + start);
        String gfxCMD = "dumpsys gfxinfo " + packageName + " framestats";
        info = new double[5];
        int vsyncOverTimes = 0; // 垂直同步次数
        try {
            Process process = Runtime.getRuntime().exec(gfxCMD);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean flag = false;
            long maxDurationFrameTime = 0;
            totalFrames = 0;
            lostFrameRate = 0;
            fps = 0;
            jumpingFrames = 0;
            long fpsRecordStartTime = 0;// #每秒帧率开始时间
            long fpsRecordEndTime = 0;//
            boolean isFisrtFrame = true;
            float SS = 0;//流畅分
            long lastDurationFrameTime = 0;
            long lastFrameStartTime = 0;
            long invalidBetweenFrameInterval = 0;
            int num = 0;
            long totalTime = 0;
            long avgTime = 0;
            int tempStuckSum = 0;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0) {
                    if (line.contains("FrameCompleted")) {
                        flag = true;
                        continue;
                    }

                    if (line.contains("View hierarchy")) {
                        break;
                    }

                    if (flag) {
                        String[] times = line.trim().split(",");
                        String frameFlag = times[0];
                        if (!"0".equals(frameFlag)) {
                            continue;
                        }
                        String startFrameTime = times[1];
                        String completedFrameTime = times[13];
                        if (isFisrtFrame) {
                            fpsRecordStartTime = Long.parseLong(startFrameTime);
                            isFisrtFrame = false;
                        }
                        //计算一帧所花费的时间
                        long onceTime = Long.parseLong(completedFrameTime) - Long.parseLong(startFrameTime);
                        totalFrames += 1; //统计总帧数
                        Log.i("GetFpsUtils", " ,onceTime:" + onceTime + ",completedFrameTime:" + completedFrameTime + ",startFrameTime:" + startFrameTime + ",totalFrames:" + totalFrames);
                        if (onceTime > fpsVsncTime) {//以Android定义的60FPS为标准
                            jumpingFrames += 1; // 统计跳帧jank数
                        }
                        if (onceTime - maxDurationFrameTime > 0) {//最大帧耗时
                            maxDurationFrameTime = onceTime;
                        }

                        if (lastFrameStartTime != 0) {
                            long betweenFrameInterval = Long.parseLong(startFrameTime) - lastFrameStartTime;
                            if (betweenFrameInterval > betweenFrameExceptionInterval) {
                                continue;
                            }
                            if (betweenFrameInterval > fpsVsncTime*3 && onceTime <= fpsVsncTime && lastDurationFrameTime <= fpsVsncTime) {
                                betweenFrameInterval = betweenFrameInterval - fpsVsncTime;
                                invalidBetweenFrameInterval += betweenFrameInterval;
                            }
                        }


                        //详细计算卡顿方法：和视觉感受差距可能有差距（保留）
                        if (num < 3) {
                            totalTime = totalTime + onceTime;
                            num++;
                        } else if (num == 3) {
                            avgTime = totalTime / 3;
                            num = 0;
                            totalTime = 0;
                        }
                        Log.i("GetFpsUtils", " ，avgTime:" + avgTime + ",num:" + num + ",totalTime:" + totalTime);
                        if (avgTime > 0) {
                            if (onceTime > 2 * avgTime && onceTime > 100000000) {
                                Log.i("GetFpsUtils---1", "卡了一次"+onceTime);
                                tempStuckSum++;
                            }
                            avgTime = 0;
                        }

                        fpsRecordEndTime = Long.parseLong(completedFrameTime);
                        lastDurationFrameTime = onceTime;
                        lastFrameStartTime = Long.parseLong(startFrameTime);


                    }
                }
            }
            float duration = (fpsRecordEndTime - fpsRecordStartTime - invalidBetweenFrameInterval) / 1000000000.0F;
            Log.i("GetFpsUtils---1", " end :" + (System.currentTimeMillis() - start) + ",invalidBetweenFrameInterval:" + invalidBetweenFrameInterval + "---" + ((fpsRecordEndTime - fpsRecordStartTime)) + "---" + duration + " ,maxDurationFrameTime:" + maxDurationFrameTime + ",vsyncOverTimes:" + vsyncOverTimes + ",totalFrames:" + totalFrames);
            if (totalFrames > 0) {
                fps = Math.round(totalFrames / duration);
                lostFrameRate = toFloat(jumpingFrames, totalFrames);
                if (fps > 60) {
                    fps = 60;
                }
                info[0] = fps;
                info[1] = Double.valueOf(lostFrameRate);//float) Commons.streamDouble(lostFrameRate * 100);
                if (maxDurationFrameTime < fpsVsncTime) {
                    maxDurationFrameTime = fpsVsncTime;
                }
                SS = (float) (fps / 60.0 * 60 + toFloat(fpsVsncTime, maxDurationFrameTime) * 20 + (1 - lostFrameRate) * 20);
                info[2] = SS;
                info[3] = totalFrames;
                info[4] = maxDurationFrameTime;
                //对比用
                if(fps>0) {
//                    if (tempStuckSum > 0||SS<10) {
//                        stuckSum++;
//                    }
                    if (lastSS > 0) {
                        float tempSS = (float) (lastSS - (lastSS * 0.5));
                        if (tempStuckSum > 1||(SS < tempSS && maxDurationFrameTime > 80000000)) {
                            stuckSum++;
                        }
                        if ((SS < tempSS||(lastSS<24&&SS<24)) && maxDurationFrameTime > 80000000) {
                            stuckSum2++;
                        }
                    }
                }
                lastSS = SS;
                Log.i("GetFpsUtils---1", " fps :" + fps +",stuckSum:"+stuckSum+",stuckSum2:"+stuckSum2 +",vsyncOverTimes:" + vsyncOverTimes + " ,jumpingFrames:" + jumpingFrames + ",lostFrameRate:" + lostFrameRate + " ,SS:" + SS);
            } else {
                System.err.println("【ERROR】无FPS信息，请确认手机正常连接或APP正常运行");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        clearFrameData(packageName);
        return info;
    }

    public static double[] getInfo2(String packageName, String className) {
        long start = System.currentTimeMillis();
        Log.i("GetFpsUtils", " start :" + start);
        String gfxCMD = "dumpsys SurfaceFlinger --latency " + packageName + "/" + className + "#0";
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
            gfxCMD = "dumpsys SurfaceFlinger --latency " + packageName + "/" + className;
        }

        Log.i("GetFpsUtils", " gfxCMD :" + gfxCMD);
        info = new double[5];
        int vsyncOverTimes = 0; // 垂直同步次数
        try {
            Process process = Runtime.getRuntime().exec(gfxCMD);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean flag = false;
            long maxDurationFrameTime = 0;
            totalFrames = 0;
            lostFrameRate = 0;
            fps = 0;
            jumpingFrames = 0;
            long fpsRecordStartTime = 0;// #每秒帧率开始时间
            long fpsRecordEndTime = 0;//
            boolean isFisrtFrame = true;
            float SS = 0;//流畅分
            long lastDurationFrameTime = 0;
            long lastFrameStartTime = 0;
            long invalidBetweenFrameInterval = 0;
            int num = 0;
            long totalTime = 0;
            long avgTime = 0;
            int tempStuckSum = 0;
            String lastLine = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0) {
                    Log.i("GetFpsUtils", " ,line:" + line);
                    if (line.contains("16666666")) {
                        flag = true;
                        continue;
                    }

                    if (flag) {
                        String[] times = line.trim().split("\t");
                        String queueBufferTime = times[0];
                        String presentFenceTime = times[1];
                        String acquireFenceTime = times[2];
                        if (queueBufferTime.equals("0")) {
                            continue;
                        }
                        if (lastLine != null) {
                            if(Long.valueOf(times[1])/Long.valueOf(times[0])>10){//异常帧过滤
                                continue;
                            }
                            String[] lastTimes = lastLine.trim().split("\t");
                            String queueBufferLastTime = lastTimes[0];
                            String presentFenceLastTime = lastTimes[1];
                            String acquireFenceLastTime = lastTimes[2];

                            String startFrameTime = presentFenceLastTime;
                            String completedFrameTime = presentFenceTime;

                            Log.i("GetFpsUtils", times[0] + "--" + times[1] + "--" + times[2]);
                            if (isFisrtFrame) {
                                fpsRecordStartTime = Long.parseLong(startFrameTime);
                                isFisrtFrame = false;
                            }
                            //计算一帧所花费的时间
                            long onceTime = Long.parseLong(completedFrameTime) - Long.parseLong(startFrameTime);
                            totalFrames += 1; //统计总帧数
                            Log.i("GetFpsUtils", " ,onceTime:" + onceTime + ",completedFrameTime:" + completedFrameTime + ",startFrameTime:" + startFrameTime + ",totalFrames:" + totalFrames);
                            if (onceTime > fpsVsncTime) {//以Android定义的60FPS为标准
                                jumpingFrames += 1; // 统计跳帧jank数
                            }
                            if (onceTime - maxDurationFrameTime > 0) {//最大帧耗时
                                maxDurationFrameTime = onceTime;
                            }

                            if (lastFrameStartTime != 0) {
                                long betweenFrameInterval = Long.parseLong(startFrameTime) - lastFrameStartTime;
                                if (betweenFrameInterval > betweenFrameExceptionInterval && lastDurationFrameTime <= fpsVsncTime) {
                                    continue;
                                }
                                if (betweenFrameInterval > fpsVsncTime*3 && onceTime <= fpsVsncTime && lastDurationFrameTime <= fpsVsncTime) {
                                    betweenFrameInterval = betweenFrameInterval - fpsVsncTime;
                                    invalidBetweenFrameInterval += betweenFrameInterval;
                                }
                            }


                            //详细计算卡顿方法：和视觉感受差距可能有差距（保留）
                            if (num < 3) {
                                totalTime = totalTime + onceTime;
                                num++;
                            } else if (num == 3) {
                                avgTime = totalTime / 3;
                                num = 0;
                                totalTime = 0;
                            }
                            Log.i("GetFpsUtils", " ，avgTime:" + avgTime + ",num:" + num + ",totalTime:" + totalTime);
                            if (avgTime > 0) {
                                if (onceTime > 2 * avgTime && onceTime > 100000000) {
                                    Log.i("GetFpsUtils---1", "卡了一次"+onceTime);
                                    tempStuckSum++;
                                }
                                avgTime = 0;
                            }

                            fpsRecordEndTime = Long.parseLong(completedFrameTime);
                            lastDurationFrameTime = onceTime;
                            lastFrameStartTime = Long.parseLong(startFrameTime);
                        }
                        lastLine = line;

                    }
                }
            }
            float duration = (fpsRecordEndTime - fpsRecordStartTime - invalidBetweenFrameInterval) / 1000000000.0F;
            Log.i("GetFpsUtils---1", " end :" + (System.currentTimeMillis() - start) + ",invalidBetweenFrameInterval:" + invalidBetweenFrameInterval + "---" + ((fpsRecordEndTime - fpsRecordStartTime)) + "---" + duration + " ,maxDurationFrameTime:" + maxDurationFrameTime + ",vsyncOverTimes:" + vsyncOverTimes + ",totalFrames:" + totalFrames);
            if (totalFrames > 0) {
                fps = Math.round(totalFrames / duration);
                lostFrameRate = toFloat(jumpingFrames, totalFrames);
                if (fps > 60) {
                    fps = 60;
                }
                info[0] = fps;
                info[1] = Double.valueOf(lostFrameRate);
                if (maxDurationFrameTime < fpsVsncTime) {
                    maxDurationFrameTime = fpsVsncTime;
                }
                SS = (float) (fps / 60.0 * 60 + toFloat(fpsVsncTime, maxDurationFrameTime) * 20 + (1 - lostFrameRate) * 20);
                info[2] = SS;
                info[3] = totalFrames;
                info[4] = maxDurationFrameTime;
                //对比用
                if(fps>0) {
                    if (lastSS > 0) {
                        float tempSS = (float) (lastSS - (lastSS * 0.5));
                        if (tempStuckSum > 0||(SS < tempSS && maxDurationFrameTime > 80000000)) {
                            stuckSum++;
                        }
                        if (SS < tempSS||(lastSS<24&&SS<24) && maxDurationFrameTime > 80000000) {
                            stuckSum2++;
                        }
                    }
                }
                lastSS = SS;
                Log.i("GetFpsUtils---1", " fps :" + fps +",stuckSum:"+stuckSum+",stuckSum2:"+stuckSum2 +",vsyncOverTimes:" + vsyncOverTimes + " ,jumpingFrames:" + jumpingFrames + ",lostFrameRate:" + lostFrameRate + " ,SS:" + SS);
            } else {
                System.err.println("【ERROR】无FPS信息，请确认手机正常连接或APP正常运行");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        clearFrameData2(packageName);
        return info;
    }

    public static Double toFloat(long numerator, long denominator) {
        DecimalFormat df = new DecimalFormat("0.00");//设置保留位数
        return Double.valueOf(df.format((float) numerator / denominator));
    }

    public static double getFps(String deviceName, String packageName) {
        return getInfo(deviceName, packageName)[0];
    }

    public static double getSS(String deviceName, String packageName) {
        if (info != null) {
            return info[2];
        }
        return 0;
    }

//    public static float getLostFrameRate(String deviceName,String packageName) {
//        if(info!=null) {
//            return info[1];
//        }
//        return 0;
//    }

    public static double getLostFrameRate(String deviceName, String packageName) {
//        return getInfo(deviceName, packageName)[1];
        if (info != null) {
            return info[1];
        }
        return 0;
    }
}
