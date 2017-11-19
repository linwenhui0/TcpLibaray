package com.hlibrary.tcp.listener;

/**
 * Created by linwenhui on 2017/8/28.
 */

public interface DataReceiver {
    void onReadSuc(byte[] readData);
}
