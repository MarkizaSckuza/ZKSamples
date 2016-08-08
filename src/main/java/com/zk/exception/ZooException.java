package com.zk.exception;

public class ZooException extends AppException {

    public ZooException(int code) {
        super(code);
    }

    public ZooException(String message) {
        super(message);
    }

    public ZooException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZooException(Throwable cause) {
        super(cause);
    }

    protected ZooException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
