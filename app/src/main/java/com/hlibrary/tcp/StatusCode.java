package com.hlibrary.tcp;

/**
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public enum StatusCode {
    SUCCESS("666", R.string.success),
    EMPTY_DATA("0", R.string.empty),
    UNKNOWN_HOST("N001", R.string.error_unknown_host),
    SOCKET_TIMEOUT("N002", R.string.error_socket_timeout),
    CONNECTION_EXCEPTION("N003", R.string.error_connection_exception),
    SOCKET_LOST("N004",R.string.error_socket_lost);

    private String statusCode;
    private int msgId;

    StatusCode(String statusCode, int msgId) {
        this.statusCode = statusCode;
        this.msgId = msgId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }
}
