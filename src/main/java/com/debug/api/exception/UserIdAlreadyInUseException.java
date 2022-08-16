package com.debug.api.exception;

import com.debug.common.StatusEnum;

public class UserIdAlreadyInUseException extends CustomAbstractException {
    public UserIdAlreadyInUseException() {
        super(StatusEnum.USERID_ALREADY_IN_USER);
    }
}
