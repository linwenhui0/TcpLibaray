package com.hlibrary.tcp.service;

import android.os.Binder;

import com.hlibrary.tcp.SocketManager;
import com.hlibrary.tcp.listener.DataReceiver;
import com.hlibrary.tcp.listener.DataSend;

import java.lang.ref.WeakReference;

/**
 * Created by linwenhui on 2017/8/30.
 */

public class SocketBinder<T extends SocketService> extends Binder {

    protected WeakReference<T> socketServiceWeakReference;

    public SocketBinder(T socketService) {
        socketServiceWeakReference = new WeakReference<T>(socketService);
    }

    public int getSocketStatus() {
        if (socketServiceWeakReference != null) {
            SocketService socketService = socketServiceWeakReference.get();
            if (socketService != null) {
                return socketService.mSocketClient.getSocketState();
            }
        }
        return SocketManager.SOCKET_UN_CONNECT;
    }

    public void send(byte[] params, DataSend dataSend) {
        if (socketServiceWeakReference != null) {
            SocketService socketService = socketServiceWeakReference.get();
            if (socketService != null) {
                socketService.mSocketClient.send(params, dataSend);
            }
        }
    }

    public void regsiterDataRecever(DataReceiver dataReceiver) {
        if (socketServiceWeakReference != null) {
            SocketService socketService = socketServiceWeakReference.get();
            if (socketService != null) {
                socketService.mSocketClient.registerDataReceiver(dataReceiver);
            }
        }
    }

    public void unRegisterDataReceiver(DataReceiver dataReceiver) {
        if (socketServiceWeakReference != null) {
            SocketService socketService = socketServiceWeakReference.get();
            if (socketService != null) {
                socketService.mSocketClient.unRegisterDataReceiver(dataReceiver);
            }
        }
    }


}
