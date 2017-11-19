package com.hlibrary.tcp.thread;

import android.content.Context;

import com.hlibrary.tcp.StatusCode;
import com.hlibrary.tcp.listener.DataRead;
import com.hlibrary.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by linwenhui on 2017/8/21.
 */

public class ReadRunnable extends BaseRunnable<DataRead> {

    final static String TAG = "ReadThread";
    private boolean keep;
    private int intervalTime = 5000;
    private ConnectRunnable.ConnectListener connectListener;

    public ReadRunnable(Context context, Socket socket) {
        super(context, socket);
    }

    public ReadRunnable setKeep(boolean keep) {
        this.keep = keep;
        return this;
    }

    public ReadRunnable setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
        return this;
    }

    public ReadRunnable setConnectListener(ConnectRunnable.ConnectListener connectListener) {
        this.connectListener = connectListener;
        return this;
    }

    @Override
    public void run() {
        if (!valib()) {
            sendMessage(MSG_SOCKET_LOST, StatusCode.SOCKET_LOST, null, new Exception());
            keep = false;
            return;
        }
        Socket socket = socketWeakReference.get();
        while (keep) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                DataInputStream is = new DataInputStream(socket.getInputStream());
                baos.reset();
                int len;
                while ((len = is.read(buffer)) > 0) {
                    Logger.i(TAG, "[接收读取数据长度] " + len);
                    baos.write(buffer, 0, len);
                    if (len < buffer.length)
                        break;
                }
                byte[] data = baos.toByteArray();
                if (data != null && data.length > 0) {
                    Logger.d(TAG, "[接收数据]，", data, Logger.TYPE.ASCII);
                    Logger.d(TAG, "[接收源数据]，", data, Logger.TYPE.CODE16);
                    sendMessage(MSG_SUCCESS, StatusCode.SUCCESS, data, null);
                }
                Logger.d(TAG, "[关闭Socket]");
                baos.close();

                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                sendMessage(MSG_SOCKET_LOST, StatusCode.UNKNOWN_HOST, null, e);
                close();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                sendMessage(MSG_SOCKET_LOST, StatusCode.UNKNOWN_HOST, null, e);
                close();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                sendMessage(MSG_SOCKET_LOST, StatusCode.SOCKET_TIMEOUT, null, e);
                close();
            } catch (ConnectException e) {
                e.printStackTrace();
                sendMessage(MSG_SOCKET_LOST, StatusCode.CONNECTION_EXCEPTION, null, e);
                close();
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage(MSG_SOCKET_LOST, StatusCode.UNKNOWN_HOST, null, e);
                close();
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(MSG_SOCKET_LOST, StatusCode.CONNECTION_EXCEPTION, null, e);
                close();
            }


        }
    }

    @Override
    protected void sendMessage(int what, StatusCode statusCode, final byte[] returnData, Throwable error) {
        switch (what) {
            case MSG_SOCKET_LOST:
                runOnUIRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (connectListener != null) {
                            connectListener.onConnectedLost();
                        }
                    }
                });

                super.sendMessage(MSG_SOCKET_LOST, statusCode, returnData, error);
                break;
            case MSG_SUCCESS:
                runOnUIRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null)
                            listener.onReadSuc(returnData);
                    }
                });
                break;
            case MSG_EMPTY:
                runOnUIRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null)
                            listener.onEmpty();
                    }
                });
                break;
            default:
                super.sendMessage(what, statusCode, returnData, error);
                break;
        }
    }


}
