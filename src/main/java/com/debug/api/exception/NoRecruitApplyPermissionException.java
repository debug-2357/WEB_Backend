package com.debug.api.exception;

import com.debug.common.StatusEnum;

public class NoRecruitApplyPermissionException extends CustomAbstractException {
    public NoRecruitApplyPermissionException() {
        super(StatusEnum.NO_RECRUIT_APPLY_PERMISSION);
    }
}
