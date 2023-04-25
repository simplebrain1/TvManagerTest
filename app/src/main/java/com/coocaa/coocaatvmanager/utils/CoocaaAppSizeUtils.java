package com.coocaa.coocaatvmanager.utils;

import android.app.ActivityManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.annotation.RequiresApi;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TODO 获取APP应用  缓存大小 数据大小 应用大小
 */

public class CoocaaAppSizeUtils {
    private static final String TAG = CoocaaAppSizeUtils.class.getSimpleName();
    private static CoocaaAppSizeUtils mApiUrl;

    private CoocaaAppSizeUtils() {
    }

    public static CoocaaAppSizeUtils getInstance() {
        if (mApiUrl == null) {
            synchronized (CoocaaAppSizeUtils.class) {
                if (mApiUrl == null) {
                    mApiUrl = new CoocaaAppSizeUtils();
                }
            }
        }
        return mApiUrl;
    }

    /**
     * TODO 获取当前应用存储信息
     */
    public void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getAppSizeO(context);
        } else {
            getAppsize(context);
        }
    }
    public void getSingleAppSize(Context context,String pkgName){
        getSingleAppSize(context,pkgName,null);
    }

    /**
     * 获取对应包名应用存储信息
     * @param context
     * @param pkgName
     */
    public void getSingleAppSize(Context context,String pkgName,OnBackListent onBackListent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            try {
                ApplicationInfo info = context.getPackageManager().getApplicationInfo(pkgName, 0);
                StorageStats storageStats = null;
                try {
                    storageStats = storageStatsManager.queryStatsForPackage(info.storageUuid, pkgName, UserHandle.getUserHandleForUid(info.uid));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long cacheQuotaBytes = getCacheQuotaBytes(storageStatsManager, info.storageUuid, info.uid);
                if (onBackListent != null) {
                    onBackListent.backData(storageStats.getCacheBytes(), storageStats.getDataBytes(), storageStats.getAppBytes(),cacheQuotaBytes);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Method method = PackageManager.class.getMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
                // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
                method.invoke(context.getPackageManager(), pkgName, new IPackageStatsObserver.Stub() {
                    @Override
                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
                        if (onBackListent != null) {
                            onBackListent.backData(pStats.cacheSize, pStats.dataSize, pStats.codeSize,0);
                        }

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Technically, we could overages as freeable on the storage settings screen.
    // If the app is using more cache than its quota, we would accidentally subtract the
    // overage from the system size (because it shows up as unused) during our attribution.
    // Thus, we cap the attribution at the quota size.
    //系统配额缓存
    public long getCacheQuotaBytes(StorageStatsManager storageStatsManager, UUID uuid, int uid){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            try {
//                Method getCacheQuotaBytes = PackageManager.class.getMethod("getCacheQuotaBytes", new Class[]{String.class, int.class});
//                long cacheQuota = (long) getCacheQuotaBytes.invoke(storageStatsManager, uuid, uid);
//                return cacheQuota;
//            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }
        return 0l;
    }

    /**
     * 获取应用的大小
     */
    /**
     * 获取应用的大小
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getAppSizeO(Context context) {
        StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            StorageStats storageStats = null;
            try {
                storageStats = storageStatsManager.queryStatsForPackage(info.storageUuid, info.packageName, UserHandle.getUserHandleForUid(info.uid));
            } catch (IOException e) {
                e.printStackTrace();
            }
            long cacheQuotaBytes = getCacheQuotaBytes(storageStatsManager, info.storageUuid, info.uid);
            if (mOnBackListent != null) {
                mOnBackListent.backData(storageStats.getCacheBytes(), storageStats.getDataBytes(), storageStats.getAppBytes(),cacheQuotaBytes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取应用大小8.0以下
     */
    public void getAppsize(Context context) {
        try {
            Method method = PackageManager.class.getMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
            // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
            method.invoke(context.getPackageManager(), context.getPackageName(), new IPackageStatsObserver.Stub() {
                @Override
                public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
                    if (mOnBackListent != null) {
                        mOnBackListent.backData(pStats.cacheSize, pStats.dataSize, pStats.codeSize,0);
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除对应包名应用缓存文件
     * @param context
     * @param pkgName
     */
    public void clearAppClearCache(Context context,String pkgName,IClearAppCacheCallBack mIClearAppCacheCallBack){
        try {
            Method method = PackageManager.class.getMethod("deleteApplicationCacheFiles", new Class[]{String.class, IPackageDataObserver.class});
            // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
            method.invoke(context.getPackageManager(), pkgName, new IPackageDataObserver.Stub() {

                @Override
                public void onRemoveCompleted(String packageName, boolean success) throws RemoteException {
                        if(mIClearAppCacheCallBack!=null){
                            mIClearAppCacheCallBack.onRemoveCompleted(packageName,success);
                        }
                }
            });
            Log.d(CoocaaStorageUtil.TAG, "clearAppClearCache ---"+pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAppClearCache(Context context,String pkgName){
        clearAppClearCache(context,pkgName,null);
    }

    public void unInstallApp(Context context,String pkgName,int flag,IPackageDeletedCallBack mIPackageDeletedCallBack){
        try {
            Method method = PackageManager.class.getMethod("deletePackage", new Class[]{String.class, IPackageDeleteObserver.class,int.class});
            method.invoke(context.getPackageManager(), pkgName, new IPackageDeleteObserver.Stub() {

                @Override
                public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                    Log.d(CoocaaStorageUtil.TAG, "packageDeleted ---"+packageName+",returnCode:"+returnCode);
                    if(mIPackageDeletedCallBack!=null){
                        mIPackageDeletedCallBack.packageDeleted(packageName,returnCode);
                    }
                }
            },flag);
            Log.d(CoocaaStorageUtil.TAG, "unInstallApp ---"+pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 卸载
     *
     * @param pkgName 需要卸载的应用包名
     */
    public static void uninstallByAdb(String pkgName) {
        try {
            //  adb shell pm uninstall com.example.test
            Runtime.getRuntime().exec("pm uninstall " + pkgName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unInstallApp(Context context,String pkgName){
        unInstallApp(context,pkgName,0,null);
    }

    /**
     * 清除用户数据
     * @param context
     * @param pkgName
     */
    public void clearAppUserData(Context context, String pkgName,IClearAppCacheCallBack mIClearAppCacheCallBack){
        ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
//            forceStopApp(context,pkgName);//应用
            Method method = ActivityManager.class.getMethod("clearApplicationUserData", new Class[]{String.class, IPackageDataObserver.class});
            method.invoke(am,pkgName, new IPackageDataObserver.Stub(){

                @Override
                public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                    Log.d(CoocaaStorageUtil.TAG, "clearAppUserData packageName:"+pkgName+" ,successed:"+succeeded);
                        if(mIClearAppCacheCallBack!=null){
                            mIClearAppCacheCallBack.onRemoveCompleted(packageName,succeeded);
                        }
                }
            }) ;
            Log.d(CoocaaStorageUtil.TAG, "clearAppUserData ---"+pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void forceStopApp(Context context, String pkgName){
//        ActivityManager am = (ActivityManager)
//                context.getSystemService(Context.ACTIVITY_SERVICE);
//        try {
//            Method method = ActivityManager.class.getMethod("forceStopPackage", new Class[]{String.class});
//            method.invoke(am,pkgName) ;
//            Log.d(CoocaaStorageUtil.TAG, "forceStopApp ---"+pkgName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        am.killBackgroundProcesses(pkgName);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getAllAppsSize(Context context){
        List<ApplicationInfo> applicationInfos =
                context.getPackageManager().getInstalledApplications(0);
        long appsTotalSize = 0l;
        for (int i = 0, size = applicationInfos.size(); i < size; i++) {
            StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            //通过包名获取uid
            StorageStats storageStats = null;
            try {
                storageStats = storageStatsManager.queryStatsForPackage(applicationInfos.get(i).storageUuid,applicationInfos.get(i).packageName,UserHandle.getUserHandleForUid(applicationInfos.get(i).uid));
            } catch (IOException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            final long dataSize = storageStats.getDataBytes();
            final long codeSize = storageStats.getAppBytes();
            final long cacheBytes = storageStats.getCacheBytes();

            long blamedSize = dataSize+codeSize+cacheBytes;

            appsTotalSize+=blamedSize;
            AppStorageEntity appStorageEntity = new AppStorageEntity(context);
            appStorageEntity.mTotalSize = blamedSize;
            appStorageEntity.mAppSize = codeSize;
            appStorageEntity.mDataSize = dataSize;
            appStorageEntity.mCacheSize = cacheBytes;
            appStorageEntity.appInfo = applicationInfos.get(i);
            appSizeMaps.put(applicationInfos.get(i).packageName,appStorageEntity);
            Log.d(CoocaaStorageUtil.TAG, "getAllAppsSize = " +applicationInfos.get(i).loadLabel(context.getPackageManager()).toString()+"---"+ applicationInfos.get(i).packageName+"---"+appStorageEntity.toString());
        }
        return appsTotalSize;
    }

    /**
     * 就近使用的应用列表
     */
    public void recentUsedApp(Context mContext){
        UsageStatsManager usageStatsManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.YEAR, -1);
            long startTime = calendar.getTimeInMillis();
            usageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
            Collection<UsageStats> result = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime).values();
            List<UsageStats> list = new ArrayList<>(result);
            Collections.sort(list, new Comparator<UsageStats>() {
                @Override
                public int compare(UsageStats o1, UsageStats o2) {
                    return Long.compare(o2.getLastTimeUsed(), o1.getLastTimeUsed());
                }
            });
            for (UsageStats usageStats :list){
                if(appSizeMaps.get(usageStats.getPackageName())==null||appSizeMaps.get(usageStats.getPackageName()).appInfo==null){
                    continue;
                }
                Log.w(TAG, "recentUsedApp usageStats = " + usageStats.getPackageName()+"---"+(appSizeMaps.get(usageStats.getPackageName()).appInfo!=null?appSizeMaps.get(usageStats.getPackageName()).appInfo.loadLabel(mContext.getPackageManager()):null));
            }
        }
    }

    public UsageStats getAppUsageStatsWithPkgName(Context mContext,String pkgName){
        UsageStatsManager usageStatsManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.YEAR, -1);
            long startTime = calendar.getTimeInMillis();
            Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
            if(usageStatsMap!=null&&usageStatsMap.containsKey(pkgName)){
                return usageStatsMap.get(pkgName);
            }
        }
        return null;
    }

    public ArrayList<UsageStats> getAppUsageStatsWithIntervalTime(Context mContext, long overTime){
        UsageStatsManager usageStatsManager = null;
        ArrayList<UsageStats> stats = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.YEAR, -1);
            long startTime = calendar.getTimeInMillis();
            Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
            if(usageStatsMap!=null&&usageStatsMap.values().size()>0){
                for (UsageStats usageStats : usageStatsMap.values()){
                    long intervalTime = System.currentTimeMillis() - usageStats.getLastTimeUsed();
                    if(intervalTime>overTime&&!isSystemApp(mContext,usageStats.getPackageName())
                            &&!usageStats.getPackageName().contains("coocaa")
                            &&!usageStats.getPackageName().contains("skyworth")){//规定时间未使用的
                        stats.add(usageStats);
                    }
                }
                return stats;
            }
        }
        return null;
    }



    public OnBackListent mOnBackListent;

    public interface OnBackListent {
        void backData(long cacheSize, long dataSize, long codeSize,long cacheQuota);
    }

    public interface IClearAppCacheCallBack{
        void onRemoveCompleted(String packageName, boolean success);
    }

    public interface IPackageDeletedCallBack{
        void packageDeleted(String packageName, int returnCode);
    }

    public CoocaaAppSizeUtils setDatasListent(OnBackListent listent) {
        mOnBackListent = listent;
        return this;
    }

    public Map<String,AppStorageEntity> appSizeMaps = new HashMap<String,AppStorageEntity>();

    public static class AppInfo{
        public String pkg;
        public AppStorageEntity mAppStorageEntity;
    }

    public static class AppStorageEntity {
        private Context context;

        public ApplicationInfo appInfo;

        private long mTotalSize;//App存储总大小

        private long mAppSize;//App代码大小

        private long mDataSize;//App数据大小

        private long mCacheSize;//App缓存大小

        public AppStorageEntity(Context context) {
            this.context = context;
        }

        public String getAppTotalSize() {
            return Formatter.formatFileSize(context,mTotalSize);
        }

        public String getAppCodeSize() {
            return Formatter.formatFileSize(context,mAppSize);
        }

        public String getAppDataSize() {
            return Formatter.formatFileSize(context,mDataSize);
        }

        public String getAppCacheSize() {
            return Formatter.formatFileSize(context,mCacheSize);
        }

        @Override
        public String toString() {
            return "AppStorageEntity{" +
                    ", mTotalSize=" + getAppTotalSize() +
                    ", mAppSize=" + getAppCodeSize() +
                    ", mDataSize=" + getAppDataSize() +
                    ", mCacheSize=" + getAppCacheSize() +
                    ",appInfo = "+appInfo+
                    '}';
        }
    }

    /**
     * 是否是系统app
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean isSystemApp(Context context, String pkgName){
        boolean isSystemApp = false;
        PackageInfo pi = null;
        try{
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(pkgName, 0);
        } catch (Throwable t){
            Log.w(TAG, "isSystemApp exception = " + t.getMessage());
        }
        // 是系统中已安装的应用
        if (pi != null && pi.applicationInfo != null){
            boolean isSysApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
            boolean isSysUpd =
                    (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1;
            isSystemApp = isSysApp || isSysUpd;
        }
        return isSystemApp;
    }
}
