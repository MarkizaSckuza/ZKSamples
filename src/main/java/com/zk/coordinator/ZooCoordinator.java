package com.zk.coordinator;

import com.zk.exception.ZooException;
import com.zk.response.ZooKeeperResponse;
import com.zk.utils.ZooUtils;
import com.zk.zoo.ZooClient;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static java.util.Objects.isNull;

@Component
@Qualifier("zoo")
public class ZooCoordinator implements Coordinator, ClusterCoordinator {

    private static Properties messagesProps;

    @Autowired
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

    @PostConstruct
    public void init() {
        connectedServerIds = new HashSet<>();
    }

    @Override
    public ZooKeeperResponse greeting() throws ZooException {
        if (zooClient == null || zooClient.getState() == ZooKeeper.States.CLOSED || zooClient.getState() == ZooKeeper.States.NOT_CONNECTED) {
            return new ZooKeeperResponse(messagesProps.getProperty("CLIENT_NOT_CONNECTED"));
        }
        return new ZooKeeperResponse(zooClient.processHelloWorld());
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
        if (!ZooUtils.isValidConnectionParameters(host, Integer.valueOf(leaderPort), Integer.valueOf(leaderElectionPort), Integer.valueOf(clientPort))) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_CONN_STR"));
        }

        ZooKeeperResponse response;
        response = new ZooKeeperResponse(zooClient.connectToCluster(ZooUtils.makeConnectionString(serverId, host, leaderPort, leaderElectionPort, clientPort)));
        connectedServerIds.add(serverId);

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
        if (!isNull(newServerId) && !isNull(host) && !isNull(leaderPort) && !isNull(leaderElectionPort) && !isNull(clientPort)
                && !ZooUtils.isValidConnectionParameters(host, Integer.valueOf(leaderPort), Integer.valueOf(leaderElectionPort), Integer.valueOf(clientPort))) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_CONN_STR"));
        }

        ZooKeeperResponse response;
        String s = zooClient.disconnectFromCluster(Integer.valueOf(serverId), ZooUtils.makeConnectionString(newServerId, host, leaderPort, leaderElectionPort, clientPort));
        response = new ZooKeeperResponse(s);
        connectedServerIds.remove(serverId);
        if (newServerId != null) connectedServerIds.add(newServerId);
        return response;
    }

    @Override
    public ZooKeeperResponse getInfo(String host, String port) {
        String hostPort = host + ":" + port;
        if (!ZooUtils.isHostPortValid(hostPort)) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_HOST_PORT"));
        }
        return new ZooKeeperResponse(zooClient.getClusterInfo(host, Integer.valueOf(port)));
    }
}
