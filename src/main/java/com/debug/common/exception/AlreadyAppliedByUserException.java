package com.debug.common.exception;

import com.debug.common.StatusEnum;

public class AlreadyAppliedByUserException extends CustomAbstractException {
    public AlreadyAppliedByUserException() {
        super(StatusEnum.ALREADY_APPLIED_BY_USER);
    }
}
