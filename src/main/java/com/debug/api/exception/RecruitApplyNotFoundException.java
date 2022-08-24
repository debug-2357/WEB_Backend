package com.debug.api.exception;

import com.debug.common.StatusEnum;

public class RecruitApplyNotFoundException extends CustomAbstractException {
    public RecruitApplyNotFoundException() {
        super(StatusEnum.RECRUIT_APPLY_NOT_FOUND);
    }
}
