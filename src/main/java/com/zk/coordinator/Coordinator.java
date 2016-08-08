package com.zk.coordinator;

import com.zk.exception.AppException;
import com.zk.response.Response;

public interface Coordinator {
    Response connectToClient(String host, String port) throws AppException;
    Response disconnectFromClient() throws AppException;
    Response connectToCluster(String sid, String host, String port1, String port2, String clientPort) throws AppException;
    Response disconnectFromCluster(String sid, String newSid, String host, String port1, String port2, String clientPort) throws AppException;
    Response getInfo(String host, String port);
}
