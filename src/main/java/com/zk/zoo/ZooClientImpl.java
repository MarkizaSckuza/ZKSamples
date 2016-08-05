package com.zk.zoo;

import com.zk.exception.ZooException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import com.zk.utils.ClusterInfoUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ZooClientImpl implements ZooClient {

    private static final int SESSION_TIMEOUT = 50000;
    private static Properties properties;
    private ZooKeeper zk;
    private String hostPort;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    static {
        try {
            Resource messagesResource = new ClassPathResource("messages.properties");
            properties = PropertiesLoaderUtils.loadProperties(messagesResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ZooClientImpl(String hostPort) {
        this.hostPort = hostPort;
    }

    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }

    public String connectToZooKeeper() throws ZooException {
        try {
            zk = new ZooKeeper(hostPort, SESSION_TIMEOUT, this);
            connectedSignal.await();
        } catch (IOException e) {
            throw new ZooException(4);
        } catch (InterruptedException e) {
            throw new ZooException(1001);
        }
        return zk.getState().toString();
    }

    public String disconnectFromZooKeeper() throws ZooException {
        if (zk != null && (zk.getState() == States.CONNECTING || zk.getState() == States.CONNECTED))
            try {
                zk.close();
                return properties.getProperty("DISCONNECTED_FROM_ZOOKEEPER");
            } catch (InterruptedException e) {
                throw new ZooException(1001);
            }
        else
            return properties.getProperty("CLIENT_NOT_CONNECTED");
    }

    public String connectToCluster(String connectionString) throws ZooException {
        try {
            zk.reconfig(null, null, connectionString, -1, new Stat());
        } catch (KeeperException e) {
            throw new ZooException(e.code().intValue());
        } catch (InterruptedException e) {
            throw new ZooException(1001);
        }
        return properties.getProperty("CONNECTED_TO_CLUSTER");
    }

    public String disconnectFromCluster(Integer serverId, String connectionString) throws ZooException {
        if (zk != null) {
            try {
                zk.reconfig(null, String.valueOf(serverId), connectionString, -1, new Stat());
            } catch (KeeperException e) {
                throw new ZooException(e.code().intValue());
            } catch (InterruptedException e) {
                throw new ZooException(1001);
            }
            return properties.getProperty("DISCONNECTED_FROM_CLUSTER");
        } else
            return properties.getProperty("SERVER_NOT_CONNECTED_TO_CLUSTER");
    }

    public String getClusterInfo(String path) {
        String info = ClusterInfoUtils.getInfo(path);
        return info != null ? info : properties.getProperty("CANNOT_GET_CLUSTER_INFO");
    }

    public ZooKeeper.States getState() {
        return zk.getState();
    }
}
