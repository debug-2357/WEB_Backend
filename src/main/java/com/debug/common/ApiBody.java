package com.debug.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiBody {

    private StatusEnum status;
    private String message;
    private Object data;

    @Builder
    public ApiBody(StatusEnum status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
