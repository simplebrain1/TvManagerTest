package com.coocaa.coocaatvmanager.dataSource;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.coocaa.coocaatvmanager.bean.AppInfo;
import com.coocaa.coocaatvmanager.utils.CoocaaAppSizeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 用来提供手机里面安装的所有应用程序信息
 *
 */
public class AppInfoProvider {

	/**
	 * 获取所有的安装的应用程序的信息
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context) {
		
		PackageManager pm = context.getPackageManager();
		// 所有的安装在系统上的应用程序的信息
		List<PackageInfo> packInfos = pm.getInstalledPackages(0);
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		
		for(PackageInfo packInfo : packInfos) {
			AppInfo appInfo = new AppInfo();
			// packageInfo 相当于一个应用程序apk包的清单文件
			String packageName = packInfo.packageName;
			Drawable icon = packInfo.applicationInfo.loadIcon(pm);
			String name = packInfo.applicationInfo.loadLabel(pm).toString();
			// 应用程序信息的标记
			int flags = packInfo.applicationInfo.flags;
			if((flags&ApplicationInfo.FLAG_SYSTEM) == 0) {
				// 用户程序
				appInfo.setUserApp(true);
			} else {
				// 系统程序
				appInfo.setUserApp(false);
			}
			if((flags&ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
				// 手机的内存里
				appInfo.setInRom(true);
			} else {
				// SD卡里
				appInfo.setInRom(false);
			}
			
			int uid = packInfo.applicationInfo.uid;	//操作系统分配给应用程序的一个固定的id，
			
//			File rcvFile = new File("/proc/uid_stat/" + uid + "/tcp_rcv");
//			File sndFile = new File("/proc/uid_stat/" + uid + "/tcp_snd");
			
			appInfo.setUid(uid);
			appInfo.setIcon(icon);
			appInfo.setName(name);
			appInfo.setPackname(packageName);
			appInfo.mUsageStats = CoocaaAppSizeUtils.getInstance().getAppUsageStatsWithPkgName(context,packageName);

			appInfos.add(appInfo);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Collections.sort(appInfos, new Comparator<AppInfo>() {
					@Override
					public int compare(AppInfo o1, AppInfo o2) {
						if(o1.mUsageStats!=null&&o2.mUsageStats!=null) {
							return Long.compare(o2.mUsageStats.getLastTimeUsed(), o1.mUsageStats.getLastTimeUsed());
						}
							return 0;
					}
				});
			}
		}
		
		return appInfos;
	}
}
