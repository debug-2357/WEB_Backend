package com.debug.common.exception;

import com.debug.common.StatusEnum;

public class UserNotFoundException extends CustomAbstractException {
    public UserNotFoundException() {
        super(StatusEnum.USER_NOT_FOUND);
    }
}
