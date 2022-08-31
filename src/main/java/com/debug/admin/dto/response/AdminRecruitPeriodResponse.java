package com.debug.admin.dto.response;

import com.debug.api.dto.response.RecruitPeriodResponse;
import com.debug.domain.entity.recruit.RecruitPeriod;

import java.time.LocalDateTime;
import java.util.List;

public class AdminRecruitPeriodResponse extends RecruitPeriodResponse {
    public AdminRecruitPeriodResponse(RecruitPeriod recruitPeriod) {
        super(recruitPeriod);
    }

    public AdminRecruitPeriodResponse(Long id, String yearOf, LocalDateTime startDate, LocalDateTime endDate, List<String> questions) {
        super(id, yearOf, startDate, endDate, questions);
    }
}
