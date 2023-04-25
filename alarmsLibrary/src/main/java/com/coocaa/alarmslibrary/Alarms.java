/*
 * Copyright (C) 2007 The Undried Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coocaa.alarmslibrary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;


public class Alarms {
	static final String TAG = "Alarms";
	static long alertMillisAfter = 5000;//定时5s测试

	public static void setNextAlert(final Context context){
		setNextAlert(context,SystemClock.elapsedRealtime()+alertMillisAfter,0);
	}

	public static void setNextAlert(final Context context,long atTimeInMillis){
		setNextAlert(context,atTimeInMillis,0);
	}

	public static void setNextAlert(final Context context,long atTimeInMillis,long intervalMillis) {
		disableAlert(context);


		Intent intent = new Intent(Constant.ALARM_ALERT_ACTION);
		intent.setPackage(context.getPackageName());
//		if(intervalMillis>0) {//要想精确间隔时间到s，可放开此参数，不用setRepeating，再结合AlarmReceiver收到提醒，做循环
//			intent.putExtra("intervalMillis", intervalMillis);
//		}
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		if(intervalMillis>0){
			//此方法intervalMillis最少要1分钟，不足1分钟会按最低1分钟算
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, atTimeInMillis,intervalMillis, sender);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				am.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, atTimeInMillis,intervalMillis, sender);
//			}
		}else {
			setExactAlarm(atTimeInMillis, am, sender);
		}

//		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);
		Log.i(TAG,"setNextAlert at " + new Date(atTimeInMillis)+",intervalMillis:"+intervalMillis);
	}

	static Method setExact = null;

	private static void setExactAlarm(long atTimeInMillis, AlarmManager am, PendingIntent sender) {
		if (Build.VERSION.SDK_INT < 19) {
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, atTimeInMillis, sender);
		}else if(Build.VERSION.SDK_INT >= 23){//低功能模式下:省电模式
			am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, atTimeInMillis, sender);
		} else {
			try {
				if (setExact == null) {
					setExact = AlarmManager.class.getMethod("setExact", int.class, long.class,
							PendingIntent.class);
					setExact.setAccessible(true);
				}
				setExact.invoke(am, AlarmManager.ELAPSED_REALTIME_WAKEUP, atTimeInMillis, sender);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void disableAlert(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(Constant.ALARM_ALERT_ACTION);
		intent.setPackage(context.getPackageName());
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
	}
}
