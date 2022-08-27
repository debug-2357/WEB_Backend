package com.debug.api.exception;

import com.debug.common.StatusEnum;

public class NonReceptionPeriodException extends CustomAbstractException {
    public NonReceptionPeriodException() {
        super(StatusEnum.NON_RECEPTION_PERIOD);
    }
}
