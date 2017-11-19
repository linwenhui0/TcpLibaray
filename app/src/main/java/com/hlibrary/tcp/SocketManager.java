package com.hlibrary.tcp;

import android.content.Context;

import com.hlibrary.tcp.listener.ConnectHandler;
import com.hlibrary.tcp.listener.DataRead;
import com.hlibrary.tcp.listener.DataReceiver;
import com.hlibrary.tcp.listener.DataSend;
import com.hlibrary.tcp.thread.ConnectRunnable;
import com.hlibrary.tcp.thread.ReadRunnable;
import com.hlibrary.tcp.thread.WriteRunnable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import model.SocketModel;

/**
 * Created by linwenhui on 2017/8/28.
 */

public class SocketManager {

    public final static int SOCKET_CONNECTED = 0x01;
    public final static int SOCKET_UN_CONNECT = 0x02;
    public final static int SOCKET_CONNECTING = 0x03;

    private ConnectHandler connectHandler;
    private List<DataReceiver> dataReceivers = new ArrayList<>();

    private Context context;
    private Socket mSocket;
    private ExecutorService pool;
    private Future connectFuthre, readFuture, writeFuture;
    private ReadRunnable readRunnable;
    private static SocketManager instance;
    private static Object lock = new Object();
    private int intervalTime = 5000;

    private SocketManager(Context context) {
        this.context = context;
        pool = Executors.newFixedThreadPool(5);
    }

    public static SocketManager getInstance(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null)
                    instance = new SocketManager(context);
            }
        }
        return instance;
    }

    /**
     * 连接 socket
     *
     * @param socketModel
     */
    public synchronized void connect(SocketModel socketModel, ConnectHandler connectHandler) {
        this.connectHandler = connectHandler;
        if (mSocket == null)
            mSocket = new Socket();
        ConnectRunnable connectRunnable = new ConnectRunnable(context, mSocket, socketModel);
        connectRunnable.setListener(connectHandler);
        connectRunnable.setConnectListener(new ConnectImp(this));
        connectFuthre = pool.submit(connectRunnable);
    }

    synchronized void connectedLost() {
        if (connectHandler != null)
            connectHandler.onConnectedLost();
    }

    /**
     * 连接状态：已连接、未连接、连接中
     *
     * @return
     */
    public synchronized int getSocketState() {
        if (mSocket != null && (mSocket.isConnected() && !mSocket.isInputShutdown() && !mSocket.isOutputShutdown()))
            return SOCKET_CONNECTED;
        else if (connectFuthre != null && (!connectFuthre.isDone() || !connectFuthre.isCancelled()))
            return SOCKET_CONNECTING;
        return SOCKET_UN_CONNECT;
    }

    /**
     * 断开连接
     */
    public synchronized void disconnect() {
        stopRead();
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册数据监听
     *
     * @param dataReceiver
     */
    public synchronized void registerDataReceiver(DataReceiver dataReceiver) {
        if (!dataReceivers.contains(dataReceiver))
            dataReceivers.add(dataReceiver);
    }

    /**
     * 取消数据监听
     *
     * @param dataReceiver
     */
    public synchronized void unRegisterDataReceiver(DataReceiver dataReceiver) {
        if (dataReceivers.contains(dataReceiver)) {
            dataReceivers.remove(dataReceiver);
        }
    }

    synchronized void startRead() {
        if (readFuture == null || (readFuture.isCancelled() || readFuture.isDone())) {
            readRunnable = new ReadRunnable(context, mSocket);
            readRunnable.setListener(new ReadImp(dataReceivers));
            readRunnable.setKeep(true).setConnectListener(new ConnectImp(this)).setIntervalTime(intervalTime);
            readFuture = pool.submit(readRunnable);
        }
    }

    synchronized void stopRead() {
        if (readRunnable != null) {
            readRunnable.setKeep(false);
            if (readFuture != null && ((!readFuture.isCancelled() || !readFuture.isDone())))
                readFuture.cancel(true);
        }
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    /**
     * 发送数据
     *
     * @param data
     * @param dataSend
     */
    public synchronized void send(byte[] data, DataSend dataSend) {
        if (writeFuture != null && (!writeFuture.isDone() || !writeFuture.isCancelled()))
            writeFuture.cancel(true);
        writeFuture = pool.submit(new WriteRunnable(context, mSocket, dataSend, data).setConnectListener(new ConnectImp(this)));
    }

    private static class ConnectImp implements ConnectRunnable.ConnectListener {

        private WeakReference<SocketManager> socketManagerWeakReference;

        public ConnectImp(SocketManager socketManager) {
            socketManagerWeakReference = new WeakReference<SocketManager>(socketManager);
        }

        @Override
        public void onConnected(Socket socket) {
            if (socketManagerWeakReference != null) {
                SocketManager socketManager = socketManagerWeakReference.get();
                if (socket != null)
                    socketManager.startRead();
            }
        }

        @Override
        public void onConnectedLost() {
            if (socketManagerWeakReference != null) {
                SocketManager socketManager = socketManagerWeakReference.get();
                if (socketManager != null) {
                    socketManager.stopRead();
                    socketManager.connectedLost();
                }
            }
        }
    }

    private static class ReadImp implements DataRead {

        private List<DataReceiver> receivers;

        public ReadImp(List<DataReceiver> receivers) {
            this.receivers = receivers;
        }

        @Override
        public void onReadSuc(byte[] readData) {
            if (receivers != null) {
                for (DataReceiver dataReceiver : receivers)
                    dataReceiver.onReadSuc(readData);
            }
        }

        @Override
        public void onEmpty() {

        }

        @Override
        public void onFailure(String code, String msg, Throwable error) {

        }
    }

}
