package com.zk.controller;

import com.zk.coordinator.ClusterCoordinator;
import com.zk.exception.AppException;
import com.zk.response.ErrorMessage;
import com.zk.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectionController {
    @Autowired
    @Qualifier("zoo")
    private ClusterCoordinator coordinator;

    @Autowired
    private Environment env;

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
    public ErrorMessage myError(AppException exception) {
        return new ErrorMessage(env.getProperty(String.valueOf(exception.getCode())));
    }
}
