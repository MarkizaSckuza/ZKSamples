package com.zk.zoo;

import com.zk.exception.ZooException;
import com.zk.hello.ZooKeeperResponse;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import com.zk.utils.PathUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.apache.zookeeper.ZooKeeper.States.CONNECTED;
import static org.apache.zookeeper.ZooKeeper.States.CONNECTING;

@Component
public class ZooControllerImpl implements ZooController {

    private static Properties messagesProps;

    private ZooClient zooClient;
    private Set<String> connectedServerIds;

    static {
        try {
            Resource messagesResource = new ClassPathResource("messages.properties");
            messagesProps = PropertiesLoaderUtils.loadProperties(messagesResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ZooControllerImpl() {
        connectedServerIds = new HashSet<>();
    }

    @Override
    public ZooKeeperResponse connectToZooKeeper(String host, String port) throws ZooException {
        if (zooClient != null && (zooClient.getState() == CONNECTED || zooClient.getState() == CONNECTING)) {
            return new ZooKeeperResponse(messagesProps.getProperty("CLIENT_CONNECTED"));
        }

        String hostPort = host + ":" + port;

        if (!PathUtils.isHostPortValid(hostPort)) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_HOST_PORT"));
        }
        return zooClient == null
                ? new ZooKeeperResponse(createAndConnectToZooKeeper(hostPort))
                : new ZooKeeperResponse(zooClient.connectToZooKeeper());
    }

    @Override
    public ZooKeeperResponse disconnectFromZooKeeper() throws ZooException {
        if (zooClient == null || zooClient.getState() == ZooKeeper.States.CLOSED || zooClient.getState() == ZooKeeper.States.NOT_CONNECTED) {
            return new ZooKeeperResponse(messagesProps.getProperty("CLIENT_NOT_CONNECTED"));
        }
        return new ZooKeeperResponse(zooClient.disconnectFromZooKeeper());
    }

    @Override
    public ZooKeeperResponse connectToCluster(String serverId, String host, String leaderPort, String leaderElectionPort, String clientPort) throws ZooException {
        if (zooClient != null && connectedServerIds.contains(serverId)) {
            return new ZooKeeperResponse(messagesProps.getProperty("SERVER_CONNECTED_TO_CLUSTER"));
        }
        if (zooClient == null) {
            return new ZooKeeperResponse(messagesProps.getProperty("CLIENT_NOT_CONNECTED"));
        }
        if (!NumberUtils.isNumber(serverId) || !NumberUtils.isNumber(leaderPort) || !NumberUtils.isNumber(leaderElectionPort) || !NumberUtils.isNumber(clientPort)) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_CONN_STR"));
        }
        if (!PathUtils.isValidConnectionParameters(host, Integer.valueOf(leaderPort), Integer.valueOf(leaderElectionPort), Integer.valueOf(clientPort))) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_CONN_STR"));
        }

        ZooKeeperResponse response;
        try {
            response = new ZooKeeperResponse(zooClient.connectToCluster(PathUtils.makeConnectionString(serverId, host, leaderPort, leaderElectionPort, clientPort)));
            connectedServerIds.add(serverId);
        } catch (ZooException e) {
            throw e;
        }

        return response;
    }

    @Override
    public ZooKeeperResponse disconnectFromCluster(String serverId, String newServerId, String host, String leaderPort, String leaderElectionPort, String clientPort) throws ZooException {
        if (zooClient == null || !connectedServerIds.contains(serverId)) {
            return new ZooKeeperResponse(messagesProps.getProperty("SERVER_NOT_CONNECTED_TO_CLUSTER"));
        }
        if (!NumberUtils.isNumber(serverId)) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_CONN_STR"));
        }
        if (!PathUtils.isNull(newServerId) && !PathUtils.isNull(host) && !PathUtils.isNull(leaderPort) && !PathUtils.isNull(leaderElectionPort) && !PathUtils.isNull(clientPort)
                && !PathUtils.isValidConnectionParameters(host, Integer.valueOf(leaderPort), Integer.valueOf(leaderElectionPort), Integer.valueOf(clientPort))) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_CONN_STR"));
        }

        ZooKeeperResponse response;
        try {
            String s = zooClient.disconnectFromCluster(Integer.valueOf(serverId), PathUtils.makeConnectionString(newServerId, host, leaderPort, leaderElectionPort, clientPort));
            response = new ZooKeeperResponse(s);
            connectedServerIds.remove(serverId);
            if (newServerId != null) connectedServerIds.add(newServerId);
        } catch (ZooException e) {
            throw e;
        }
        return response;
    }

    @Override
    public ZooKeeperResponse getInfo(String host, String port) {
        String hostPort = host + ":" + port;
        if (!PathUtils.isHostPortValid(hostPort)) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_HOST_PORT"));
        }
        return new ZooKeeperResponse(getClusterInfo(hostPort));
    }

    private String createAndConnectToZooKeeper(String hostPort) throws ZooException {
        zooClient = new ZooClientImpl(hostPort);
        return zooClient.connectToZooKeeper();
    }

    private String getClusterInfo(String path) {
        return zooClient.getClusterInfo(path);
    }
}
