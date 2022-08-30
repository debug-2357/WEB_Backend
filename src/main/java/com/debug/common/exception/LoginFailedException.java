package com.debug.common.exception;

import com.debug.common.StatusEnum;

public class LoginFailedException extends CustomAbstractException {
    public LoginFailedException() {
        super(StatusEnum.UNMATCHED_ID_OR_PASSWORD);
    }
}
