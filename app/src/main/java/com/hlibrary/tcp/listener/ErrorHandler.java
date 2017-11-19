package com.hlibrary.tcp.listener;

/**
 * Created by linwenhui on 2017/3/3.
 */

public interface ErrorHandler {

    /**
     * 请求失败
     *
     * @param code  状态码
     * @param msg   状态信息
     * @param error 异常对象
     */
    void onFailure(String code, String msg, Throwable error);
}
