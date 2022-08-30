package com.debug.common.exception;

import com.debug.common.StatusEnum;

public class UnexpiredAccessTokenException extends CustomAbstractException {
    public UnexpiredAccessTokenException() {
        super(StatusEnum.NOT_EXPIRED_ACCESS_TOKEN_YET);
    }
}
