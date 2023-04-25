package com.coocaa.coocaatvmanager;

import android.util.Log;

public class InstrumentedActivity extends MainActivity{
        public static String TAG = "InstrumentedActivity";

        private FinishListener mListener;

        public void setFinishListener(FinishListener listener) {
            mListener = listener;
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG + ".InstrumentedActivity", "onDestroy()");
            if (mListener != null) {
                mListener.onActivityFinished();
            }
        }

}
