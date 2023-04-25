//package com.coocaa.coocaatvmanager.utils;
//
//import android.util.Log;
//
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//
//@Aspect
//public class TrackHelper {
//    private static final String TAG = "TrackHelper";
//
////    @Before("execution(* com.coocaa.coocaatvmanager..*.*(..))")
////    public void onActivityCalled(JoinPoint  joinPoint) throws Throwable {
////        Signature signature = joinPoint.getSignature();
////        String name = signature.toShortString();
////        Log.i(TAG,  " signature" +   signature.getName()+" ,name:"+name);
////    }
//
//    @Around("execution(* com.coocaa.coocaatvmanager.MainActivity.**(..))")
//    public void onActivityCalled(ProceedingJoinPoint joinPoint) throws Throwable {
//        Signature signature = joinPoint.getSignature();
//        String name = signature.toShortString();
//        Log.i(TAG,  " signature" +   signature.getName()+" ,name:"+name);
//        long time = System.currentTimeMillis();
//        try {
//            joinPoint.proceed();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        Log.i(TAG, name + " cost" +     (System.currentTimeMillis() - time));
//    }
//
////    @Around("execution(* com.coocaa.coocaatvmanager.utils.CoocaaStorageUtil.*(..))")
////    public void getTime(ProceedingJoinPoint joinPoint) {
////        Signature signature = joinPoint.getSignature();
////        String name = signature.toShortString();
////        Log.i(TAG,  " signature" +   signature.getName()+" ,name:"+name);
////        long time = System.currentTimeMillis();
////        try {
////            joinPoint.proceed();
////        } catch (Throwable throwable) {
////            throwable.printStackTrace();
////        }
////        Log.i(TAG, name + " cost" +     (System.currentTimeMillis() - time));
////    }
//
//    /*@Before("execution(* com.irisleon.fridge.*.*.*(..)) || execution(* com.irisleon.fridge.*.*(..))")
//    public void DebugFunctionLog(JoinPoint joinPoint) throws Throwable {
//        Log.d(TAG, "----> FuncTrace:" + joinPoint.getSignature());
//        Log.d(TAG, "        At:" + joinPoint.getSourceLocation());
//        for (Object item : joinPoint.getArgs()) {
//            if (item != null) {
//                Log.d(TAG, "        Arg:" + item.toString());
//            }
//        }
//    }
//
//    @AfterReturning(pointcut = "execution(* com.irisleon.fridge.*.*.*(..)) || execution(* com.irisleon.fridge.*.*(..))",
//            returning = "retVal")
//    public void DebugReturnLog(JoinPoint joinPoint, Object retVal) throws Throwable {
//        Log.d(TAG, "<---- FuncTrace:" + joinPoint.getSignature());
//        if (retVal != null) {
//            Log.d(TAG, "        Return:" + retVal);
//        }
//    }*/
//}