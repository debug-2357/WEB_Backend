package com.debug.common.exception;

import com.debug.common.StatusEnum;

public class InvalidAccessTokenException extends CustomAbstractException {
    public InvalidAccessTokenException() {
        super(StatusEnum.INVALID_ACCESS_TOKEN);
    }
}
