package com.debug.common.exception;

import com.debug.common.StatusEnum;

public class RefreshTokenNotFoundException extends CustomAbstractException {
    public RefreshTokenNotFoundException() {
        super(StatusEnum.REFRESH_TOKEN_NOT_FOUND);
    }
}
