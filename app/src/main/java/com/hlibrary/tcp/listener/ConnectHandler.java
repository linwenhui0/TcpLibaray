package com.hlibrary.tcp.listener;

/**
 * Created by linwenhui on 2017/4/21.
 */

public interface ConnectHandler extends ErrorHandler {

    void onConnected();

    void onConnectedLost();

}
