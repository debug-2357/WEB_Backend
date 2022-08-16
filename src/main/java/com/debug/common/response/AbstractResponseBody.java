package com.debug.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class AbstractResponseBody {
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
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
