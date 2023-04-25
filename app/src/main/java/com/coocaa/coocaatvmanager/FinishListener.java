package com.coocaa.coocaatvmanager;

public interface FinishListener {
    void onActivityFinished();
    void dumpIntermediateCoverage(String filePath);
}
