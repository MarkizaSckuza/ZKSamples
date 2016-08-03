package zoo;

import exception.ZooException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import utils.ClusterInfoUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Server implements Watcher {

    private static final int SESSION_TIMEOUT = 7000;
    private static Properties properties;
    private ZooKeeper zk;
    private String hostPort;
    private String member;
    private String serverId;
    private boolean isConnected;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    static {
        try {
            Resource messagesResource = new ClassPathResource("messages.properties");
            properties = PropertiesLoaderUtils.loadProperties(messagesResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server(String hostPort) {
        this.hostPort = hostPort;
        this.serverId = Integer.toHexString(new Random().nextInt());
        this.member = "server." + serverId + "=" + hostPort;
    }

    @Override
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
                return "DISCONNECTED_FROM_ZOOKEEPER";
            } catch (InterruptedException e) {
                throw new ZooException(1001);
            }
        else
            return properties.getProperty("SERVER_NOT_CONNECTED");
    }

    public String connectToCluster() throws ZooException {
        if (zk == null || zk.getState() == States.CLOSED || zk.getState() == States.NOT_CONNECTED) {
            connectToZooKeeper();
        }
        try {
            zk.reconfig(null, null, member, -1, new Stat());
        } catch (KeeperException e) {
            throw new ZooException(e.code().intValue());
        } catch (InterruptedException e) {
            throw new ZooException(1001);
        }
        isConnected = true;
        return properties.getProperty("CONNECTED_TO_CLUSTER");
    }

    public String disconnectFromCluster() throws ZooException {
        if (zk != null && isConnected) {
            try {
                zk.reconfig(null, serverId, null, -1, new Stat());
            } catch (KeeperException e) {
                throw new ZooException(e.code().intValue());
            } catch (InterruptedException e) {
                throw new ZooException(1001);
            }
            isConnected = false;
            return properties.getProperty("DISCONNECTED_FROM_CLUSTER");
        } else
            return properties.getProperty("SERVER_NOT_CONNECTED_TO_CLUSTER");
    }

    public String getClusterInfo() {
        String info = ClusterInfoUtils.getInfo(hostPort);
        return info != null ? info : properties.getProperty("CANNOT_GET_CLUSTER_INFO");
    }

    public boolean isConnected() {
        return isConnected;
    }

    public ZooKeeper.States getState() {
        return zk.getState();
    }
}
