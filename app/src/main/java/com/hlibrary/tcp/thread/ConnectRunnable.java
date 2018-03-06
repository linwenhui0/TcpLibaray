package com.hlibrary.tcp.thread;

import android.content.Context;

import com.hlibrary.tcp.StatusCode;
import com.hlibrary.tcp.listener.ConnectHandler;
import com.hlibrary.util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import model.SocketModel;

/**
 * Created by linwenhui on 2017/8/28.
 */

public class ConnectRunnable extends BaseRunnable<ConnectHandler> {

    private final static String TAG = "ConnectRunnable";

    private Socket mSocket;
    private SocketModel mSocketModel;
    private ConnectListener connectListener;

    public ConnectRunnable(Context context, Socket mSocket, SocketModel socketModel) {
        super(context, mSocket);
        this.mSocket = mSocket;
        this.mSocketModel = socketModel;
    }

    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    @Override
    public void run() {
        Logger.getInstance().d(TAG, "[地址] ip = " + mSocketModel.getIp() + " port = " + mSocketModel.getPort());
        InetSocketAddress address = new InetSocketAddress(mSocketModel.getIp(), mSocketModel.getPort());
        try {
            mSocket.setReceiveBufferSize(65535);
            Logger.getInstance().d(TAG, " setReceiveBufferSize(65535) ");
            mSocket.setSendBufferSize(4096);
            Logger.getInstance().d(TAG, " setSendBufferSize(4096) ");
            Logger.getInstance().d(TAG, "[超时时间]" + mSocketModel.getTimeout());
            mSocket.connect(address, mSocketModel.getTimeout());
            Logger.getInstance().d(TAG, "[连接成功]");
            sendMessage(MSG_SUCCESS, StatusCode.SUCCESS, null, null);
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage(MSG_FAILED, StatusCode.UNKNOWN_HOST, null, e);
            close();
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(MSG_FAILED, StatusCode.CONNECTION_EXCEPTION, null, e);
            close();
        }
    }

    @Override
    protected void sendMessage(int what, StatusCode statusCode, final byte[] returnData, Throwable error) {
        if (what == MSG_SUCCESS) {
            if (connectListener != null)
                connectListener.onConnected(mSocket);

            if (listener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnected();
                    }
                });
            }
        } else
            super.sendMessage(what, statusCode, null, error);
    }

    public interface ConnectListener {
        void onConnected(Socket socket);
        void onConnectedLost();
    }

}
