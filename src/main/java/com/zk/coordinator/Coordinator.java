package com.zk.coordinator;

import com.zk.exception.AppException;
import com.zk.response.Response;

public interface Coordinator {
    Response greeting() throws AppException;
}
