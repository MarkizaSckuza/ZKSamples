package com.zk.response;

public class ZooKeeperResponse implements Response {

    private final String message;

    public ZooKeeperResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
