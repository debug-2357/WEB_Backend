package com.debug.common.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class AbstractResponseBody {
    LocalDateTime timestamp;
    int status;
    String statusDetail;
    String code;
    String message;

    public AbstractResponseBody(int status, String statusDetail, String code, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.statusDetail = statusDetail;
        this.code = code;
        this.message = message;
    }
}
