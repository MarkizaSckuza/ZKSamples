package com.zk.zoo;

import com.zk.exception.ZooException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper.States;

public interface ZooClient extends Watcher {
    String processHelloWorld() throws ZooException;

    String connectToCluster(String connectionString) throws ZooException;

    String disconnectFromCluster(Integer serverId, String connectionString) throws ZooException;

    String getClusterInfo(String host, int port);

    States getState();
}
