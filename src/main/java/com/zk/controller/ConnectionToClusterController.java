package com.zk.controller;

import com.zk.exception.ZooException;
import com.zk.hello.ZooKeeperResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zk.zoo.ZooController;

import java.io.IOException;
import java.util.Properties;

@RestController
public class ConnectionToClusterController {
    private static Properties errorsProps;

    @Autowired
    private ZooController zooController;

    static {
        try {
            Resource errorsResource = new ClassPathResource("errors.properties");
            errorsProps = PropertiesLoaderUtils.loadProperties(errorsResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/connect")
    public ZooKeeperResponse connect(@RequestParam(value = "host") String host, @RequestParam(value = "port") String port) throws  ZooException {
        return zooController.connectToZooKeeper(host, port);
    }

    @RequestMapping("/disconnect")
    public ZooKeeperResponse disconnect() throws ZooException {
        return zooController.disconnectFromZooKeeper();
    }

    @RequestMapping("/cluster/connect")
    public ZooKeeperResponse connectToCluster(@RequestParam(value = "sid") String sid,
                                              @RequestParam(value = "host") String host,
                                              @RequestParam(value = "port1") String port1,
                                              @RequestParam(value = "port2") String port2,
                                              @RequestParam(value = "cliPort") String clientPort) throws ZooException {
        return zooController.connectToCluster(sid, host, port1, port2, clientPort);
    }

    @RequestMapping("/cluster/disconnect")
    public ZooKeeperResponse disconnectFromCluster(@RequestParam(value = "sid") String sid,
                                                   @RequestParam(value = "newSid", required = false) String newSid,
                                                   @RequestParam(value = "host", required = false) String host,
                                                   @RequestParam(value = "port1", required = false) String port1,
                                                   @RequestParam(value = "port2", required = false) String port2,
                                                   @RequestParam(value = "cliPort", required = false) String clientPort) throws ZooException {
        return zooController.disconnectFromCluster(sid, newSid, host, port1, port2, clientPort);
    }

    @RequestMapping(path = "/cluster/info")
    public ZooKeeperResponse getInfo(@RequestParam(value = "host") String host, @RequestParam(value = "port") String port) throws ZooException {
        return zooController.getInfo(host, port);
    }

    @ExceptionHandler(ZooException.class)
    public ZooKeeperResponse myError(ZooException exception) {
        return new ZooKeeperResponse(errorsProps.getProperty(String.valueOf(exception.getCode())));
    }
}
