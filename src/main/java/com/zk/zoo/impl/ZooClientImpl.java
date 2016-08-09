package com.zk.zoo.impl;

import com.zk.exception.ZooException;
import com.zk.utils.ZooUtils;
import com.zk.zoo.ZooClient;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

@Component
public class ZooClientImpl implements ZooClient {

    private static final int SESSION_TIMEOUT = 50000;
    private ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    @Autowired
    private Environment env;

    @Value("zookeeper.host")
    private String host;

    @Value("zookeeper.port")
    private String port;

    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }

    @Override
    public String processHelloWorld() throws ZooException {
        try {
            return new String(getZooKeeper().getData("/hello", false, new Stat()), Charset.defaultCharset());
        } catch (KeeperException e) {
            throw new ZooException(e.code().intValue());
        } catch (InterruptedException e) {
            throw new ZooException(1001);
        }
    }

    public ZooKeeper getZooKeeper() throws ZooException {
        if (zk == null) {
            try {
                zk = new ZooKeeper(host + ":" + port, SESSION_TIMEOUT, this);
                connectedSignal.await();
                initData();
            } catch (IOException e) {
                throw new ZooException(4);
            } catch (InterruptedException e) {
                throw new ZooException(1001);
            } catch (KeeperException e) {
                throw new ZooException(e.code().intValue());
            }
        }
        return zk;
    }

    public String connectToCluster(String connectionString) throws ZooException {
        try {
            zk.reconfig(null, null, connectionString, -1, new Stat());
        } catch (KeeperException e) {
            throw new ZooException(e.code().intValue());
        } catch (InterruptedException e) {
            throw new ZooException(1001);
        }
        return env.getProperty("CONNECTED_TO_CLUSTER");
    }

    public String disconnectFromCluster(Integer serverId, String connectionString) throws ZooException {
        try {
            getZooKeeper().reconfig(null, String.valueOf(serverId), connectionString, -1, new Stat());
        } catch (KeeperException e) {
            throw new ZooException(e.code().intValue());
        } catch (InterruptedException e) {
            throw new ZooException(1001);
        }
        return env.getProperty("DISCONNECTED_FROM_CLUSTER");
    }

    public String getClusterInfo(String host, int port) {
        String info = ZooUtils.getInfo(host, port);
        return info != null ? info : env.getProperty("CANNOT_GET_CLUSTER_INFO");
    }

    public ZooKeeper.States getState() {
        return zk.getState();
    }

    private void initData() throws KeeperException, InterruptedException {
        zk.create("/hello", "Hello World!".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }
}
