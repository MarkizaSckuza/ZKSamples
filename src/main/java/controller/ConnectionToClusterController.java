package controller;

import exception.ZooException;
import hello.ZooKeeperResponse;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import utils.UriUtils;
import zoo.Server;
import zoo.ZooServer;

import java.io.IOException;
import java.util.Properties;

import static org.apache.zookeeper.ZooKeeper.States.CONNECTED;

@RestController
public class ConnectionToClusterController {
    private static Properties errorsProps;
    private static Properties messagesProps;

    private ZooServer server;

    static {
        try {
            Resource errorsResource = new ClassPathResource("errors.properties");
            Resource messagesResource = new ClassPathResource("messages.properties");
            errorsProps = PropertiesLoaderUtils.loadProperties(errorsResource);
            messagesProps = PropertiesLoaderUtils.loadProperties(messagesResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/connect")
    public ZooKeeperResponse connect(@RequestParam(value = "hostPort") String hostPort) throws  ZooException {
        if (server != null && server.getState() == CONNECTED) {
            return new ZooKeeperResponse(messagesProps.getProperty("SERVER_CONNECTED"));
        }
        if (!UriUtils.isHostPortValid(hostPort)) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_HOST_PORT"));
        }
        return server == null ? new ZooKeeperResponse(createAndConnectToZooKeeper(hostPort)) : new ZooKeeperResponse(server.connectToZooKeeper());
    }

    @RequestMapping("/disconnect")
    public ZooKeeperResponse disconnect() throws ZooException {
        if (server == null || server.getState() == ZooKeeper.States.CLOSED || server.getState() == ZooKeeper.States.NOT_CONNECTED) {
            return new ZooKeeperResponse(messagesProps.getProperty("SERVER_NOT_CONNECTED"));
        }
        return new ZooKeeperResponse(server.disconnectFromZooKeeper());
    }

    @RequestMapping("/cluster/connect")
    public ZooKeeperResponse connectToCluster(@RequestParam(value = "hostPort") String hostPort) throws ZooException {
        if (server != null && server.isConnected()) {
            return new ZooKeeperResponse(messagesProps.getProperty("SERVER_CONNECTED_TO_CLUSTER"));
        }
        if (!UriUtils.isHostPortValid(hostPort)) {
            return new ZooKeeperResponse(messagesProps.getProperty("WRONG_HOST_PORT"));
        }
        return server == null ? new ZooKeeperResponse(createAndConnectToCluster(hostPort)) : new ZooKeeperResponse(server.connectToCluster());
    }

    @RequestMapping("/cluster/disconnect")
    public ZooKeeperResponse disconnectFromCluster() throws ZooException {
        if (server == null || !server.isConnected()) {
            return new ZooKeeperResponse(messagesProps.getProperty("SERVER_NOT_CONNECTED_TO_CLUSTER"));
        }
        return new ZooKeeperResponse(server.disconnectFromCluster());
    }

    @RequestMapping(path = "/cluster/info")
    public ZooKeeperResponse getInfo() throws ZooException {
        if (server == null || !server.isConnected()) {
            return new ZooKeeperResponse(messagesProps.getProperty("SERVER_NOT_CONNECTED_TO_CLUSTER"));
        }
        return new ZooKeeperResponse(getClusterInfo());
    }

    @ExceptionHandler(ZooException.class)
    public ZooKeeperResponse myError(Exception exception) {
        return new ZooKeeperResponse(errorsProps.getProperty(String.valueOf(((ZooException) exception).getCode())));
    }

    private String createAndConnectToZooKeeper(String hostPort) throws ZooException {
        server = new Server(hostPort);
        return server.connectToZooKeeper();
    }

    private String createAndConnectToCluster(String hostPort) throws ZooException {
        server = new Server(hostPort);
        return server.connectToCluster();
    }

    private String getClusterInfo() {
        return server.getClusterInfo();
    }
}
