package com.debug.admin.dto.response;

import com.debug.api.dto.response.RecruitApplyResponse;
import com.debug.api.dto.response.RecruitPeriodResponse;
import com.debug.api.dto.response.UserResponse;
import com.debug.domain.entity.recruit.RecruitApply;

import java.time.LocalDateTime;
import java.util.Map;

public class AdminRecruitApplyResponse extends RecruitApplyResponse {
    public AdminRecruitApplyResponse(RecruitApply recruitApply) {
        super(recruitApply);
    }

    public AdminRecruitApplyResponse(Long id, UserResponse user, RecruitPeriodResponse recruitPeriod, Map<String, String> content, String isPass, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        super(id, user, recruitPeriod, content, isPass, createdDate, modifiedDate);
    }
}
