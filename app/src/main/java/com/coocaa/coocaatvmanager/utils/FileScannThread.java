package com.coocaa.coocaatvmanager.utils;

import java.io.File;

public class FileScannThread extends Thread{

        public FileScannThread(File rootDir, OnScanFileThreadListener l){
            _onScanFileThreadListener = l;
            this.rootDir = rootDir;
        }

        @Override
        public void run() {
            super.run();

            findInDir(rootDir);

            if (_onScanFileThreadListener!=null){
                _onScanFileThreadListener.completed();
            }
        }

        public void findInDir(File dir){

            for (File f:dir.listFiles()){
                if (f.isFile()){
                    String path = f.getAbsolutePath();


                    if (_onScanFileThreadListener!=null){
                        _onScanFileThreadListener.foundFle(f);

                        if (f.length()>1024*1024*10) {
                            _onScanFileThreadListener.foundBigFile(f);
                        }
                    }
                }else if (f.isDirectory()){
                    findInDir(f);
                }
            }
        }

        private File rootDir = null;

        private OnScanFileThreadListener _onScanFileThreadListener = null;

        public static interface OnScanFileThreadListener {

            void foundFle(File f);
            void foundBigFile(File f);
            void completed();
        }
}
