package com.hlibrary.tcp.listener;

/**
 * Created by linwenhui on 2017/8/28.
 */

public interface DataRead extends ErrorHandler {

    void onReadSuc(byte[] readData);

    void onEmpty();

}
