package com.debug.api.dto.response;

import com.debug.domain.entity.recruit.RecruitApply;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class RecruitApplyResponse {

    private final Long id;
    private final UserResponse user;
    private final RecruitPeriodResponse recruitPeriod;
    private final Map<String, String> content;
    private final String isPass;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;

    public RecruitApplyResponse(RecruitApply recruitApply) {
        this.id = recruitApply.getId();
        this.user = new UserResponse(recruitApply.getUser());
        this.recruitPeriod = new RecruitPeriodResponse(recruitApply.getRecruitPeriod());
        this.content = recruitApply.getContent();
        this.isPass = convertIsPass(recruitApply.getIsPass());
        this.createdDate = recruitApply.getCreatedDate();
        this.modifiedDate = recruitApply.getModifiedDate();
    }

    @Builder
    public RecruitApplyResponse(Long id, UserResponse user, RecruitPeriodResponse recruitPeriod, Map<String, String> content, String isPass, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.user = user;
        this.recruitPeriod = recruitPeriod;
        this.content = content;
        this.isPass = isPass;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    private String convertIsPass(Boolean isPass) {
        if (isPass == null) return "검토중";

        return isPass ? "합격" : "불합격";
    }
}
