package com.zk.controller;

import com.zk.coordinator.Coordinator;
import com.zk.exception.AppException;
import com.zk.response.ErrorMessage;
import com.zk.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    @Autowired
    private Environment env;

    @Autowired
    private Coordinator coordinator;

    @RequestMapping("/hello")
    public Response greeting() throws AppException {
        return coordinator.greeting();
    }

    @ExceptionHandler(AppException.class)
    public ErrorMessage myError(AppException exception) {
        return new ErrorMessage(env.getProperty(String.valueOf(exception.getCode())));
    }
}
