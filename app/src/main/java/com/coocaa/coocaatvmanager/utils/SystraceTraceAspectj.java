package com.coocaa.coocaatvmanager.utils;

import android.os.Build;
import android.os.Trace;
import android.util.Log;

import androidx.core.os.TraceCompat;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

//@Aspect
//public class SystraceTraceAspectj {
//
//    private static final String TAG = "SystraceTraceAspectj";
//
//    @Before("execution(* **(..))")
//    public void before(JoinPoint joinPoint) {
////        TraceCompat.beginSection(joinPoint.getSignature().toString());
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
////            Trace.beginSection(joinPoint.getSignature().toString());
////        }
//        Signature signature = joinPoint.getSignature();
//        String name = signature.toShortString();
//        Log.i(TAG,  " signature" +   joinPoint.getSignature().getName()+" ,name:"+name);
//    }
//
//    @After("execution(* **(..))")
//    public void after() {
////        TraceCompat.endSection();
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
////            Trace.endSection();
////        }
//    }
//}
