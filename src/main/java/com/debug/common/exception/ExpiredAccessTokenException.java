package com.debug.common.exception;

import com.debug.common.StatusEnum;

public class ExpiredAccessTokenException extends CustomAbstractException {
    public ExpiredAccessTokenException() {
        super(StatusEnum.EXPIRED_ACCESS_TOKEN);
    }
}
