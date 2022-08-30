package com.debug.api.dto.response;

import com.debug.domain.entity.recruit.RecruitPeriod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class RecruitPeriodResponse {

    private final Long id;
    private final String yearOf;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final List<String> questions;

    public RecruitPeriodResponse(RecruitPeriod recruitPeriod) {
        this.id = recruitPeriod.getId();
        this.yearOf = recruitPeriod.getYearOf();
        this.startDate = recruitPeriod.getStartDate();
        this.endDate = recruitPeriod.getEndDate();
        this.questions = recruitPeriod.getQuestions();
    }

    @Builder
    public RecruitPeriodResponse(Long id, String yearOf, LocalDateTime startDate, LocalDateTime endDate, List<String> questions) {
        this.id = id;
        this.yearOf = yearOf;
        this.startDate = startDate;
        this.endDate = endDate;
        this.questions = questions;
    }
}
