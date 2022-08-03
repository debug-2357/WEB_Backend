package com.debug.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiBody {

    private String status;
    private String message;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime timestamp;
    private Object data;


    @Builder
    public ApiBody(HttpStatus status, MessageEnum message, Object data) {
        this.status = status.getReasonPhrase();
        this.message = message.getMessage();
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
}
