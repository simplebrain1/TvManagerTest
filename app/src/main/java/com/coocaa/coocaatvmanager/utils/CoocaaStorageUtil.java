package com.coocaa.coocaatvmanager.utils;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.RequiresApi;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 内部存储工具类
 */
public class CoocaaStorageUtil {

    public final static String TAG = "coocaa_storage";

    private static Map<String, AppStorageEntity> appSizeMaps = new HashMap<String, AppStorageEntity>();//每个应用的存储信息

    private static volatile CoocaaStorageUtil mCoocaaStorageUtil;

    private CoocaaStorageUtil() {
    }

    public static CoocaaStorageUtil getInstance() {
        if (mCoocaaStorageUtil == null) {
            synchronized (CoocaaAppSizeUtils.class) {
                if (mCoocaaStorageUtil == null) {
                    mCoocaaStorageUtil = new CoocaaStorageUtil();
                }
            }
        }
        return mCoocaaStorageUtil;
    }

    public void init(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            queryAppStorageTotalSize(context);
        }
    }

    public StorageEntity queryWithStorageManager(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        int version = Build.VERSION.SDK_INT;
        StorageEntity storageEntity = new StorageEntity(context);
        queryAppStorageTotalSize(context);
        if (version < Build.VERSION_CODES.M) {//小于6.0，只能查到共享卷即除系统外的内存大小
            try {
                Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumeList");
                StorageVolume[] volumeList = (StorageVolume[]) getVolumeList.invoke(storageManager);
                long totalSize = 0, usedSize = 0;
                long storageTotalSize = getStorageTotalSize();
                long appStorageTotalSize = getAppStorageTotalSize();
                if (volumeList != null) {
                    Method getPathFile = null;
                    Method isRemovableMethod = null;
                    Method getVolumeState = null;
                    for (StorageVolume volume : volumeList) {
                        if(isRemovableMethod == null) {
                            isRemovableMethod = volume.getClass().getMethod("isRemovable", new Class[0]);
                        }
                        if(getVolumeState == null) {
                            getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
                        }
                        if (getPathFile == null) {
                            getPathFile = volume.getClass().getDeclaredMethod("getPathFile");
                        }
                        boolean isRemovable = ((Boolean)isRemovableMethod.invoke(volume,new Object[0])).booleanValue();
                        File file = (File) getPathFile.invoke(volume);
                        String state = (String) getVolumeState.invoke(storageManager, file.getAbsolutePath());
                        Log.d(TAG, "storageEntity isRemovable = " + isRemovable+",state:"+state);
                        if("mounted".equals(state)&&!isRemovable) {
                            totalSize += file.getTotalSpace();
                            long fileUsedSize = file.getTotalSpace() - file.getFreeSpace();
                            usedSize += fileUsedSize;
                        }
                    }
                    storageEntity.totalSize = storageTotalSize;
                    storageEntity.systemSize = storageTotalSize - totalSize;//totalSize没有包括系统占用的存储
                    storageEntity.appSpaceSize = appStorageTotalSize;
                    storageEntity.otherSpaceSize = usedSize - storageEntity.appSpaceSize;
                    storageEntity.usedSize = usedSize + storageEntity.systemSize;
                    storageEntity.freeSize = storageTotalSize - storageEntity.usedSize;
                }
                Log.d(TAG, "storageEntity = " + storageEntity.toString());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            queryAppStorageTotalSize(context);
            try {
                Method getVolumes = StorageManager.class.getDeclaredMethod("getVolumes");//6.0
                List<Object> getVolumeInfo = (List<Object>) getVolumes.invoke(storageManager);
                long total = 0L, used = 0L, systemSize = 0L;
                long appStorageTotalSize = getAppStorageTotalSize();
                for (Object obj : getVolumeInfo) {
                    Log.i(TAG, " volumes:" + obj);
                    Field getType = obj.getClass().getField("type");
                    Field getId = obj.getClass().getField("id");
                    Field getMountFlags = obj.getClass().getField("mountFlags");
                    int type = getType.getInt(obj);
                    String id = (String) getId.get(obj);
                    int mountFlags = getMountFlags.getInt(obj);
                    Log.d(TAG, "type: " + type+",id:"+id+" ,mountFlags:"+mountFlags);
                    if (type == 1||"private".equals(id)) {//TYPE_PRIVATE

                        long totalSize = 0L;

                        //获取内置内存总大小
                        if (version >= Build.VERSION_CODES.O) {//8.0
                            Method getFsUuid = obj.getClass().getDeclaredMethod("getFsUuid");
                            String fsUuid = (String) getFsUuid.invoke(obj);
                            totalSize = getTotalSize(context, fsUuid);//8.0 以后使用
                        } else if (version >= Build.VERSION_CODES.N_MR1) {//7.1.1
                            Method getPrimaryStorageSize = StorageManager.class.getMethod("getPrimaryStorageSize");//5.0 6.0 7.0没有
                            totalSize = (long) getPrimaryStorageSize.invoke(storageManager);
                        }

                        Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
                        boolean readable = (boolean) isMountedReadable.invoke(obj);
                        if (readable) {
                            Method file = obj.getClass().getDeclaredMethod("getPath");
                            File f = (File) file.invoke(obj);
                            Log.d(TAG, "f: " + f.getAbsolutePath());

                            if (totalSize == 0) {
                                totalSize = f.getTotalSpace();
                            }
                            systemSize = totalSize - f.getTotalSpace();
                            used += totalSize - f.getFreeSpace();
                            total += totalSize;
                        }

                    }
//                    else if (type == 0) {//TYPE_PUBLIC
//                        //外置存储
//                        Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
//                        boolean readable = (boolean) isMountedReadable.invoke(obj);
//                        if (readable) {
//                            Method file = obj.getClass().getDeclaredMethod("getPath");
//                            File f = (File) file.invoke(obj);
//                            used += f.getTotalSpace() - f.getFreeSpace();
//                            total += f.getTotalSpace();
//                        }
//                    } else if (type == 2) {//TYPE_EMULATED//模拟存储
//
//                    }
                }

                storageEntity.totalSize = total;
                storageEntity.usedSize = used;
                storageEntity.freeSize = total - used;
                storageEntity.systemSize = systemSize;
                storageEntity.appSpaceSize = appStorageTotalSize;//used - systemSize;
                storageEntity.otherSpaceSize = used - systemSize - storageEntity.appSpaceSize;

                Log.e(TAG, " total:" + Formatter.formatFileSize(context, total));
                Log.e(TAG, " used:" + Formatter.formatFileSize(context, used));
                Log.e(TAG, " freeSize:" + Formatter.formatFileSize(context, storageEntity.freeSize));
                Log.e(TAG, " storageEntity.systemSize:" + Formatter.formatFileSize(context, storageEntity.systemSize));
                Log.e(TAG, " storageEntity.appSpaceSize:" + Formatter.formatFileSize(context, storageEntity.appSpaceSize));
                Log.e(TAG, " storageEntity.otherSpaceSize:" + Formatter.formatFileSize(context, storageEntity.otherSpaceSize));
            } catch (SecurityException e) {
                Log.e(TAG, " no permission.PACKAGE_USAGE_STATS");
            } catch (Exception e) {
                e.printStackTrace();
                storageEntity = queryWithStatFs(context);
            }
        }
        return storageEntity;
    }

    private long getAppStorageTotalSize(){
        long mAppsTotalSize = 0l;
        if(appSizeMaps!=null&&appSizeMaps.size()>0){
            for (AppStorageEntity mAppStorageEntity:appSizeMaps.values()) {
                mAppsTotalSize += mAppStorageEntity.mTotalSize;
            }
        }
        return mAppsTotalSize;
    }

    /**
     * 获取应用统计大小
     */
    private void queryAppStorageTotalSize(Context context){
        List<ApplicationInfo> applicationInfos =
                context.getPackageManager().getInstalledApplications(0);
        if(appSizeMaps!=null&&!appSizeMaps.isEmpty()){
            appSizeMaps.clear();
        }
        for (int i = 0, size = applicationInfos.size(); i < size; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                //通过包名获取uid
                StorageStats storageStats = null;
                try {
                    storageStats = storageStatsManager.queryStatsForPackage(applicationInfos.get(i).storageUuid, applicationInfos.get(i).packageName, UserHandle.getUserHandleForUid(applicationInfos.get(i).uid));
                } catch (IOException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                final long dataSize = storageStats.getDataBytes();
                final long codeSize = storageStats.getAppBytes();
                final long cacheBytes = storageStats.getCacheBytes();

                long blamedSize = dataSize + codeSize + cacheBytes;

                AppStorageEntity appStorageEntity = new AppStorageEntity(context);
                appStorageEntity.mTotalSize = blamedSize;
                appStorageEntity.mAppSize = codeSize;
                appStorageEntity.mDataSize = dataSize;
                appStorageEntity.mCacheSize = cacheBytes;
                appStorageEntity.appInfo = applicationInfos.get(i);
                appSizeMaps.put(applicationInfos.get(i).packageName, appStorageEntity);
                Log.d(CoocaaStorageUtil.TAG, "getAllAppsSize = " + applicationInfos.get(i).loadLabel(context.getPackageManager()).toString() + "---" + applicationInfos.get(i).packageName + "---" + appStorageEntity.toString());
            } else {//8.0以下
                try {
                    Method method = PackageManager.class.getMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
                    // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
                    int finalI = i;
                    method.invoke(context.getPackageManager(), context.getPackageName(), new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
                            if (succeeded) {
                                long blamedSize = pStats.dataSize + pStats.codeSize + pStats.cacheSize;

                                AppStorageEntity appStorageEntity = new AppStorageEntity(context);
                                appStorageEntity.mTotalSize = blamedSize;
                                appStorageEntity.mAppSize = pStats.codeSize;
                                appStorageEntity.mDataSize = pStats.dataSize;
                                appStorageEntity.mCacheSize = pStats.cacheSize;
                                appStorageEntity.appInfo = applicationInfos.get(finalI);
                                appSizeMaps.put(applicationInfos.get(finalI).packageName, appStorageEntity);
                                Log.d(CoocaaStorageUtil.TAG, "getAllAppsSize onGetStatsCompleted = " + applicationInfos.get(finalI).loadLabel(context.getPackageManager()).toString() + "---" + applicationInfos.get(finalI).packageName + "---" + appStorageEntity.toString());
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取内置总存储
     */
    private static long getStorageTotalSize(){
       return roundStorageSize(Environment.getDataDirectory().getTotalSpace()+Environment.getRootDirectory().getTotalSpace());
    }

    /**
     * Round the given size of a storage device to a nice round power-of-two
     * value, such as 256MB or 32GB. This avoids showing weird values like
     * "29.5GB" in UI.
     *
     */
    private static long roundStorageSize(long size) {
        long val = 1;
        long pow = 1;
        while ((val * pow) < size) {
            val <<= 1;
            if (val > 512) {
                val = 1;
                pow *= 1000;
            }
        }
        return val * pow;
    }

    /**
     * 此方法不会把系统空间算进来的
     */
    public static StorageEntity queryWithStatFs(Context context) {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        //存储块
        long blockCount = statFs.getBlockCount();
        //块大小
        long blockSize = statFs.getBlockSize();
        //可用块数量
        long availableCount = statFs.getAvailableBlocks();
        //剩余块数量，注：这个包含保留块（including reserved blocks）即应用无法使用的空间
        long freeBlocks = statFs.getFreeBlocks();

        long totalSize = blockSize * blockCount;//不包括系统

        long freeSize = blockSize * freeBlocks;

        long userSize = totalSize - freeSize;//应用使用的存储

        StorageEntity storageEntity = new StorageEntity(context);
        long storageTotalSize = getStorageTotalSize();//包括系统
        storageEntity.totalSize = storageTotalSize;
        storageEntity.systemSize = storageTotalSize - totalSize;
        storageEntity.usedSize = userSize + storageEntity.systemSize;
        storageEntity.freeSize = storageEntity.totalSize - storageEntity.usedSize;
        storageEntity.appSpaceSize = userSize;

        return storageEntity;
    }


    /**
     * API 26 android O
     * 获取总共容量大小，包括系统大小
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private static long getTotalSize(Context context, String fsUuid) {
        try {
            UUID id;
            if (fsUuid == null) {
                id = StorageManager.UUID_DEFAULT;
            } else {
                id = UUID.fromString(fsUuid);
            }
            StorageStatsManager stats = context.getSystemService(StorageStatsManager.class);
            return stats.getTotalBytes(id);
        } catch (NoSuchFieldError | NoClassDefFoundError | NullPointerException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public static class StorageEntity {
        private Context context;

        public long totalSize;//存储总大小

        public long usedSize;//已经使用的存储大小

        public long freeSize;//剩余可用的存储大小

        public long systemSize;//系统占用大小

        public long appSpaceSize;//应用占用大小

        public long otherSpaceSize;//其它占用空间大小

        private StorageEntity(Context context) {
            this.context = context;
        }

        public String getUsedSize() {
            return Formatter.formatFileSize(context,usedSize);
        }

        public String getFreeSize() {
            return Formatter.formatFileSize(context,freeSize);
        }

        public String getOtherSpaceSize(){
            return Formatter.formatFileSize(context,otherSpaceSize);
        }

        public String getSystemSize() {
            if (systemSize <= 0) {
                return "未知";
            }
            return Formatter.formatFileSize(context,systemSize);
        }

        public String getAppSpaceSize() {
            return Formatter.formatFileSize(context,appSpaceSize);
        }

        public String getTotalSize() {
            return Formatter.formatFileSize(context,totalSize);
        }

        @Override
        public String toString() {
            return "StorageEntity{" +
                    "context=" + context +
                    ", totalSize=" + getTotalSize() +
                    ", usedSize=" + getUsedSize() +
                    ", freeSize=" + getFreeSize() +
                    ", systemSize=" + getSystemSize() +
                    ", appSpaceSize=" + getAppSpaceSize() +
                    ", otherSpaceSize=" + getOtherSpaceSize() +
                    '}';
        }
    }

    /**
     * 应用存储实体
     */
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
}
