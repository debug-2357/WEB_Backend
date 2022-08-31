package com.debug.admin.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminRecruitPeriodRequest {
    private String yearOf;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> questions;
}
