package com.zk.zoo;

import com.zk.exception.ZooException;
import com.zk.hello.ZooKeeperResponse;

public interface ZooController {

    ZooKeeperResponse connectToZooKeeper(String host, String port) throws ZooException;
    ZooKeeperResponse disconnectFromZooKeeper() throws ZooException;
    ZooKeeperResponse connectToCluster(String sid, String host, String port1, String port2, String clientPort) throws ZooException;
    ZooKeeperResponse disconnectFromCluster(String sid, String newSid, String host, String port1, String port2, String clientPort) throws ZooException;
    ZooKeeperResponse getInfo(String host, String port);
}
