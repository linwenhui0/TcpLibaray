package com.hlibrary.tcp.thread;

import android.content.Context;

import com.hlibrary.tcp.StatusCode;
import com.hlibrary.tcp.listener.DataSend;
import com.hlibrary.util.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by linwenhui on 2017/8/21.
 */

public class WriteRunnable extends BaseRunnable<DataSend> {

    final static String TAG = "WriteThread";
    private byte[] data;
    private ConnectRunnable.ConnectListener connectListener;

    public WriteRunnable(Context context, Socket socket, DataSend handler, byte[] data) {
        super(context, socket);
        setListener(handler);
        this.data = data;
    }

    public WriteRunnable setConnectListener(ConnectRunnable.ConnectListener connectListener) {
        this.connectListener = connectListener;
        return this;
    }

    @Override
    public void run() {
        if (!valib()) {
            sendMessage(MSG_FAILED, StatusCode.SOCKET_LOST, null, new Exception());
            return;
        }
        Socket socket = socketWeakReference.get();
        try {
            Logger.getInstance().d(TAG, "[发送数据]，", data, Logger.TYPE.ASCII);
            Logger.getInstance().d(TAG, "[发送16进制数据]，", data, Logger.TYPE.CODE16);
            BufferedOutputStream os = new BufferedOutputStream(socket.getOutputStream());
            os.write(data);
            os.flush();
            sendMessage(MSG_SUCCESS, StatusCode.SUCCESS, null, null);
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

    @Override
    protected void sendMessage(int what, StatusCode statusCode, byte[] returnData, Throwable error) {
        if (what == MSG_SUCCESS) {
            runOnUIRunnable(new Runnable() {
                @Override
                public void run() {
                    if (listener != null)
                        listener.onSendSuc();
                }
            });
        } else if (what == MSG_SOCKET_LOST) {
            runOnUIRunnable(new Runnable() {
                @Override
                public void run() {
                    if (connectListener != null) {
                        connectListener.onConnectedLost();
                    }
                }
            });

            super.sendMessage(MSG_SOCKET_LOST, statusCode, returnData, error);
        } else
            super.sendMessage(what, statusCode, returnData, error);
    }


}
