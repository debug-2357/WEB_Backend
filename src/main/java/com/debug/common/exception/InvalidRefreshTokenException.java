package com.debug.common.exception;

import com.debug.common.StatusEnum;

public class InvalidRefreshTokenException extends CustomAbstractException {
    public InvalidRefreshTokenException() {
        super(StatusEnum.INVALID_REFRESH_TOKEN);
    }
}
