package com.debug.api.exception;

import com.debug.common.StatusEnum;

public class RecruitPeriodNotFoundException extends CustomAbstractException {
    public RecruitPeriodNotFoundException() {
        super(StatusEnum.RECRUIT_PERIOD_NOT_FOUND);
    }
}
