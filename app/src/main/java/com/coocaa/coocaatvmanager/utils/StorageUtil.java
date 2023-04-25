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
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * 内部存储工具类
 */
public class StorageUtil {

    public final static String TAG = "StorageUtil";

    private static final Map<String, AppStorageEntity> appSizeMaps = new HashMap<>();//每个应用的存储信息

    private static volatile StorageUtil mStorageUtil;

    private StorageEntity mStorageEntity;

    private StorageUtil() {
    }

    public static StorageUtil getInstance() {
        if (mStorageUtil == null) {
            synchronized (StorageUtil.class) {
                if (mStorageUtil == null) {
                    mStorageUtil = new StorageUtil();
                }
            }
        }
        return mStorageUtil;
    }

    public StorageEntity queryWithStorageManager(Context context) {
        if (mStorageEntity != null) {
            return mStorageEntity;
        }
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        int version = Build.VERSION.SDK_INT;
        StorageEntity storageEntity = new StorageEntity(context);
        queryAppStorageTotalSize(context);
        if (version < Build.VERSION_CODES.M) {//小于6.0，只能查到共享卷即除系统外的内存大小
            try {
                Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumeList");
                StorageVolume[] volumeList = (StorageVolume[]) getVolumeList.invoke(storageManager);
                long totalSize = 0,free = 0;
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
                            free += file.getUsableSpace();//file.getFreeSpace();
                        }
                    }
                    storageEntity.totalSize = storageTotalSize;
                    storageEntity.systemSize = storageTotalSize - totalSize;//totalSize没有包括系统占用的存储
                    storageEntity.appSpaceSize = appStorageTotalSize;
                    storageEntity.usedSize = storageTotalSize - free;// storageEntity.systemSize;
                    storageEntity.otherSpaceSize = storageEntity.usedSize - storageEntity.systemSize - storageEntity.appSpaceSize;
                    storageEntity.freeSize = free;//storageTotalSize - storageEntity.usedSize;
                    Log.d(TAG, "storageEntity--1 = " + storageEntity.toString());
                }
                if(totalSize<= 0){
                    storageEntity = queryWithStatFs(context);
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
            try {
                Method getVolumes = StorageManager.class.getDeclaredMethod("getVolumes");//6.0
                List<Object> getVolumeInfo = (List<Object>) getVolumes.invoke(storageManager);
                long free = 0L,dataSize = 0L;
                long appStorageTotalSize = getAppStorageTotalSize();
                long total = getStorageTotalSize();
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
                        Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
                        boolean readable = (boolean) isMountedReadable.invoke(obj);
                        if (readable) {
                            Method file = obj.getClass().getDeclaredMethod("getPath");
                            File f = (File) file.invoke(obj);
//                            Log.d(TAG, "f: " + f.getAbsolutePath()+",total:"+ Formatter.formatFileSize(context,f.getTotalSpace())+",free:"+ Formatter.formatFileSize(context,f.getFreeSpace()));
                            Log.d(TAG, "f: " + f.getAbsolutePath()+",total:"+ f.getTotalSpace()+",free:"+ f.getFreeSpace()+"---"+f.getUsableSpace());


                            dataSize += f.getTotalSpace();//可读分区大小
                            free += f.getUsableSpace();//f.getFreeSpace();//分区剩余大小
                        }

                    }
                }

                storageEntity.totalSize = total;
                storageEntity.usedSize = total - free;
                storageEntity.freeSize = free ;
                storageEntity.appSpaceSize = appStorageTotalSize;
                storageEntity.systemSize = total - dataSize;
                storageEntity.otherSpaceSize = storageEntity.usedSize - storageEntity.systemSize - storageEntity.appSpaceSize;
                if(dataSize <= 0 || total<=0){//存储卷类型不对标(不同机型可能存储类型不对标)
                    storageEntity = queryWithStatFs(context);
                }
                Log.e(TAG, " total:" + Formatter.formatFileSize(context, total));
                Log.e(TAG, " used:" + Formatter.formatFileSize(context, storageEntity.usedSize));
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
        mStorageEntity = storageEntity;
        return storageEntity;
    }

    private long getAppStorageTotalSize() {
        long mAppsTotalSize = 0l;
        if (appSizeMaps != null && appSizeMaps.size() > 0) {
            for (AppStorageEntity mAppStorageEntity : appSizeMaps.values()) {
                mAppsTotalSize += mAppStorageEntity.mTotalSize;
            }
        }
        return mAppsTotalSize;
    }
    /**
     * 获取应用统计大小
     */
    public void queryAppStorageTotalSize(Context context) {
        List<ApplicationInfo> applicationInfos =
                context.getPackageManager().getInstalledApplications(0);
        Log.i(TAG," queryAppStorageTotalSize applicationInfos.size:"+applicationInfos.size());
        if (appSizeMaps != null && !appSizeMaps.isEmpty()) {
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
                Log.d(StorageUtil.TAG, "getAllAppsSize = " + applicationInfos.get(i).loadLabel(context.getPackageManager()).toString() + "---" + applicationInfos.get(i).packageName + "---" + appStorageEntity.toString());
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
                                Log.i(TAG," queryAppStorageTotalSize finalI:"+finalI);
                                appSizeMaps.put(applicationInfos.get(finalI).packageName, appStorageEntity);
                                Log.d(StorageUtil.TAG, "getAllAppsSize onGetStatsCompleted = " + applicationInfos.get(finalI).loadLabel(context.getPackageManager()).toString() + "---" + applicationInfos.get(finalI).packageName + "---" + appStorageEntity.toString());
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
    private static long getStorageTotalSize() {
        long totalStorageSize = roundStorageSize(Environment.getDataDirectory().getTotalSpace() + Environment.getRootDirectory().getTotalSpace());
        totalStorageSize =  (totalStorageSize/1000000000)*1024*1024*1024;//以1024为1M的计算单位
        return totalStorageSize;
    }

    /**
     * Round the given size of a storage device to a nice round power-of-two
     * value, such as 256MB or 32GB. This avoids showing weird values like
     * "29.5GB" in UI.
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
    public StorageEntity queryWithStatFs(Context context) {
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

        long freeSize = blockSize*availableCount;//blockSize * freeBlocks;

        StorageEntity storageEntity = new StorageEntity(context);
        long storageTotalSize = getStorageTotalSize();//包括系统
        Log.d(StorageUtil.TAG,"storageTotalSize:"+storageTotalSize+",totalSize:"+totalSize+",free:"+freeSize);
        storageEntity.totalSize = storageTotalSize;
        storageEntity.systemSize = storageTotalSize - totalSize;
        storageEntity.usedSize = storageTotalSize - freeSize;
        storageEntity.freeSize = freeSize;
        storageEntity.appSpaceSize = getAppStorageTotalSize();
        storageEntity.otherSpaceSize = storageEntity.usedSize - storageEntity.systemSize - storageEntity.appSpaceSize;
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

        public long otherSpaceSize;//其它占用空间大小(并到系统占用里)

        private StorageEntity(Context context) {
            this.context = context;
        }

        public String getUsedSize() {
            return Formatter.formatFileSize(context, usedSize);
        }

        public String getFreeSize() {
            return Formatter.formatFileSize(context, freeSize);
        }

        private String getOtherSpaceSize() {
            return Formatter.formatFileSize(context, otherSpaceSize);
        }

        public String getSystemSize() {
            if (systemSize <= 0) {
                return "未知";
            }
            return Formatter.formatFileSize(context, systemSize+otherSpaceSize);//其它占用并到系统占用里
        }

        public String getAppSpaceSize() {
            return Formatter.formatFileSize(context, appSpaceSize);
        }

        public String getTotalSize() {
            return Formatter.formatFileSize(context, totalSize);
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
            return Formatter.formatFileSize(context, mTotalSize);
        }

        public String getAppCodeSize() {
            return Formatter.formatFileSize(context, mAppSize);
        }

        public String getAppDataSize() {
            return Formatter.formatFileSize(context, mDataSize);
        }

        public String getAppCacheSize() {
            return Formatter.formatFileSize(context, mCacheSize);
        }

        @Override
        public String toString() {
            return "AppStorageEntity{" +
                    ", mTotalSize=" + getAppTotalSize() +
                    ", mAppSize=" + getAppCodeSize() +
                    ", mDataSize=" + getAppDataSize() +
                    ", mCacheSize=" + getAppCacheSize() +
                    ",appInfo = " + appInfo +
                    '}';
        }
    }

    public static class Formatter {
        /** * get file format size * * @param context context * @param roundedBytes file size * @return file format size (like 2.12k) */
        public static String formatFileSize(Context context, long roundedBytes) {
            return formatFileSize(context, roundedBytes, false,1, Locale.US);
        }
        public static String formatFileSize(Context context, long roundedBytes,int flag) {
            return formatFileSize(context, roundedBytes, false,flag, Locale.US);
        }

        public static String formatFileSize(Context context, long roundedBytes, Locale locale) {
            return formatFileSize(context, roundedBytes, false, 1,locale);
        }


        private static String formatFileSize(Context context, long roundedBytes, boolean shorter, int flags,Locale locale) {
            if (context == null) {
                return "";
            }
            final int unit = (flags != 0) ? 1024 : 1000;
            float result = roundedBytes;
            String suffix = "B";
            if (result > 900) {
                suffix = "KB";
                result = result / unit;
            }
            if (result > 900) {
                suffix = "MB";
                result = result / unit;
            }
            if (result > 900) {
                suffix = "GB";
                result = result / unit;
            }
            if (result > 900) {
                suffix = "TB";
                result = result / unit;
            }
            if (result > 900) {
                suffix = "PB";
                result = result / unit;
            }
            String value;
            if (result < 1) {
                value = String.format(locale, "%.2f", result);
            } else if (result < 10) {
                if (shorter) {
                    value = String.format(locale, "%.1f", result);
                } else {
                    value = String.format(locale, "%.2f", result);
                }
            } else if (result < 100) {
                if (shorter) {
                    value = String.format(locale, "%.0f", result);
                } else {
                    value = String.format(locale, "%.2f", result);
                }
            } else {
                value = String.format(locale, "%.0f", result);
            }
            return String.format("%s%s", value, suffix);
        }

    }
}
