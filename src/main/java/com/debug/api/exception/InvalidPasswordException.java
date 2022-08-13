package com.debug.api.exception;

import com.debug.common.StatusEnum;

public class InvalidPasswordException extends CustomAbstractException {
    public InvalidPasswordException() {
        super(StatusEnum.INVALID_PASSWORD);
    }
}
