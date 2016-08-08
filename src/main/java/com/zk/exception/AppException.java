package com.zk.exception;

public class AppException extends Exception {

    private int code;

    public AppException() {
    }

    public AppException(int code) {
        if (code < 0) {
            code *= -1;
        }
        this.code = code;
    }

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppException(Throwable cause) {
        super(cause);
    }

    public AppException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getCode() {
        return code;
    }
}
