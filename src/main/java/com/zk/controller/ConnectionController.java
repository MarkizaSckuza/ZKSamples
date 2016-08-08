package com.zk.controller;

import com.zk.coordinator.Coordinator;
import com.zk.exception.AppException;
import com.zk.response.Response;
import com.zk.response.ZooKeeperResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Properties;

//TODO открыть гайд по именованию рест контроллеров Спринг РЕСТ
@RestController
public class ConnectionController {
    private static Properties errorsProps;

    @Autowired
    @Qualifier("zoo")
    private Coordinator coordinator;

    static {
        try {
            Resource errorsResource = new ClassPathResource("errors.properties");
            errorsProps = PropertiesLoaderUtils.loadProperties(errorsResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/connect")
    public Response connect(@RequestParam(value = "host") String host, @RequestParam(value = "port") String port) throws  AppException {
        return coordinator.connectToClient(host, port);
    }

    @RequestMapping("/disconnect")
    public Response disconnect() throws AppException {
        return coordinator.disconnectFromClient();
    }

    @RequestMapping("/cluster/connect")
    public Response connectToCluster(@RequestParam(value = "sid") String sid,
                                     @RequestParam(value = "host") String host,
                                     @RequestParam(value = "port1") String port1,
                                     @RequestParam(value = "port2") String port2,
                                     @RequestParam(value = "cliPort") String clientPort) throws AppException {
        return coordinator.connectToCluster(sid, host, port1, port2, clientPort);
    }

    @RequestMapping("/cluster/disconnect")
    public Response disconnectFromCluster(@RequestParam(value = "sid") String sid,
                                                   @RequestParam(value = "newSid", required = false) String newSid,
                                                   @RequestParam(value = "host", required = false) String host,
                                                   @RequestParam(value = "port1", required = false) String port1,
                                                   @RequestParam(value = "port2", required = false) String port2,
                                                   @RequestParam(value = "cliPort", required = false) String clientPort) throws AppException {
        return coordinator.disconnectFromCluster(sid, newSid, host, port1, port2, clientPort);
    }

    @RequestMapping(path = "/cluster/info")
    public Response getInfo(@RequestParam(value = "host") String host, @RequestParam(value = "port") String port) throws AppException {
        return coordinator.getInfo(host, port);
    }

    @ExceptionHandler(AppException.class)
    public Response myError(AppException exception) {
        return new ZooKeeperResponse(errorsProps.getProperty(String.valueOf(exception.getCode())));
    }
}
