package com.coocaa.coocaatvmanager.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import java.util.List;

/**
 * 锁屏服务
 * @author hello
 *
 */
public class AutoClearService extends Service {
	
	private ScreenOffReceiver receiver;
	private ActivityManager am;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		receiver = new ScreenOffReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private class ScreenOffReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 屏幕锁屏了
			List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
			for(RunningAppProcessInfo info : infos) {
				am.killBackgroundProcesses(info.processName);
			}
		}
		
	}
}
