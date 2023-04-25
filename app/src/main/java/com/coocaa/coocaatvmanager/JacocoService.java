package com.coocaa.coocaatvmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class JacocoService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        JacocoUtils.start(this,getApplicationContext());
        super.onCreate();
        Log.i("JacocoService","JacocoService onCreate");
//        Instrumentation instrumentation = new Instrumentation();
//        instrumentation.
    }

    @Override
    public void onDestroy() {
        JacocoUtils.stop(getApplicationContext());
        super.onDestroy();
        Log.i("JacocoService","JacocoService onDestroy");
    }
}
