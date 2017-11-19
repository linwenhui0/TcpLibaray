package com.hlibrary.tcp.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hlibrary.tcp.StatusCode;
import com.hlibrary.tcp.listener.ErrorHandler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;

/**
 * Created by linwenhui on 2017/8/21.
 */

public abstract class BaseRunnable<T extends ErrorHandler> implements Runnable {

    public final static int MSG_SUCCESS = 0x100;
    public final static int MSG_FAILED = 0x101;
    public final static int MSG_EMPTY = 0x102;
    public final static int MSG_SOCKET_LOST = 0x103;

    protected static Handler mainHandler = new Handler(Looper.getMainLooper());
    protected WeakReference<Socket> socketWeakReference;
    protected Context context;
    protected T listener;

    public BaseRunnable(Context context, Socket socket) {
        this.context = context;
        this.socketWeakReference = new WeakReference<Socket>(socket);
    }

    public T getListener() {
        return listener;
    }

    public void setListener(T listener) {
        this.listener = listener;
    }

    protected boolean valib() {
        if (socketWeakReference == null)
            return false;
        else if (socketWeakReference.get() != null) {
            Socket socket = socketWeakReference.get();
            if (socket.isClosed() || !socket.isConnected() || socket.isOutputShutdown() || socket.isInputShutdown())
                return false;
            return true;
        }
        return false;
    }

    public void close() {
        if (socketWeakReference != null) {
            Socket socket = socketWeakReference.get();
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void sendMessage(int what, final StatusCode statusCode, final byte[] returnData, final Throwable error) {
        if (listener == null)
            return;
        if (what == MSG_FAILED) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(statusCode.getStatusCode(), getString(statusCode), error);
                }
            });
        }
    }

    public void runOnUIRunnable(Runnable runnable) {
        mainHandler.post(runnable);
    }

    protected String getString(StatusCode error) {
        return context.getResources().getString(error.getMsgId());
    }

}
