package com.hlibrary.tcp.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.hlibrary.tcp.SocketManager;
import com.hlibrary.tcp.listener.ConnectHandler;

import model.SocketModel;

/**
 * Created by linwenhui on 2017/8/21.
 */

public abstract class SocketService<T extends SocketBinder> extends Service implements ConnectHandler {
    protected SocketManager mSocketClient;
    protected AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mSocketClient = SocketManager.getInstance(this);
        mSocketClient.setIntervalTime(getIntervalTime());

        Intent intent = new Intent(this, SocketService.class);
        PendingIntent sender = PendingIntent.getService(this, 0, intent, 0);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), getHartIntervalTime(), sender);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mSocketClient.getSocketState() == SocketManager.SOCKET_UN_CONNECT)
            mSocketClient.connect(getSocketModel(), this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public abstract T onBind(Intent intent);

    @Override
    public void onDestroy() {
        mSocketClient.disconnect();
        super.onDestroy();
    }

    /**
     * 服务端的数据
     *
     * @return
     */
    public abstract SocketModel getSocketModel();

    /**
     * 隔多久从socket接口读取数据
     *
     * @return
     */
    public abstract int getIntervalTime();

    /**
     * 检测socket长连接心动时间间隔
     *
     * @return
     */
    public abstract long getHartIntervalTime();

}
