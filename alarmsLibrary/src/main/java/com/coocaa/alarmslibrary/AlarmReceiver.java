package com.coocaa.alarmslibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "receive the broadcast " + intent.toUri(0)+ Build.VERSION.SDK_INT+"--"+context.getApplicationInfo().targetSdkVersion);
        if (Constant.ALARM_ALERT_ACTION.equals(intent.getAction())) {
            Log.i(TAG, "receive the broadcast------start  AlarmRemindService");

            //todo：启动你的服务或是别的动作
//           Intent playAlarm = new Intent(context, TestAlarmsService.class);
            Class<?> eventCls = null;
            try {
                eventCls = Class.forName("com.coocaa.coocaatvmanager.MainActivity");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Intent playAlarm = new Intent(context, eventCls);
            playAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(playAlarm);
//            context.startService(playAlarm);


            //不用am.setRepeating时，可以根据intervalMillis传参做间隔循环（比较精确）
            int interval = (int) intent.getLongExtra("intervalMillis", 0);
            Log.i(TAG, "interval:"+interval);
            if(interval!=0) {//循环
                Alarms.setNextAlert(context, SystemClock.elapsedRealtime()+interval,interval);
            }
        }
    }
}
