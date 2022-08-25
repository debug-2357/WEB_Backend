package com.debug.api.controller.recruit;

import com.debug.api.dto.response.RecruitApplyResponse;
import com.debug.api.dto.response.RecruitPeriodResponse;
import com.debug.api.service.recruit.RecruitApplyService;
import com.debug.api.service.recruit.RecruitPeriodService;
import com.debug.common.StatusEnum;
import com.debug.common.response.SuccessResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruit")
@RequiredArgsConstructor
public class RecruitController {

    private final RecruitPeriodService recruitPeriodService;
    private final RecruitApplyService recruitApplyService;

    @GetMapping("/periods")
    public ResponseEntity<SuccessResponseBody> findAllRecruitPeriods() {
        List<RecruitPeriodResponse> recruitPeriodResponses = recruitPeriodService.findAll();

        if (recruitPeriodResponses.isEmpty()) {
            return SuccessResponseBody.toResponseEntity(StatusEnum.NO_RECRUIT_PERIOD_CONTENT, null);
        }

        return SuccessResponseBody.toResponseEntity(StatusEnum.GET_RECRUIT_PERIOD, recruitPeriodResponses);
    }

    @GetMapping("/periods/recent")
    public ResponseEntity<SuccessResponseBody> getRecentRecruitPeriod() {
        RecruitPeriodResponse recruitPeriodResponse = recruitPeriodService.getRecentRecruitPeriod();

        return SuccessResponseBody.toResponseEntity(StatusEnum.GET_RECENT_RECRUIT_PERIOD, recruitPeriodResponse);
    }

    @GetMapping("/periods/{recruitPeriodId}")
    public ResponseEntity<SuccessResponseBody> findByRecruitPeriodId(@PathVariable Long recruitPeriodId) {
        RecruitPeriodResponse recruitPeriodResponse = recruitPeriodService.findByRecruitPeriodId(recruitPeriodId);

        return SuccessResponseBody.toResponseEntity(StatusEnum.GET_RECRUIT_PERIOD, recruitPeriodResponse);
    }

    @GetMapping("/periods/applies")
    public ResponseEntity<SuccessResponseBody> findAllMyApplies(@AuthenticationPrincipal UserDetails userDetails) {
        List<RecruitApplyResponse> applyResponses = recruitApplyService.getAppliesByUserId(userDetails.getUsername());

        if (applyResponses.isEmpty()) {
            return SuccessResponseBody.toResponseEntity(StatusEnum.NO_RECRUIT_APPLY_CONTENT, null);
        }

        return SuccessResponseBody.toResponseEntity(StatusEnum.GET_RECRUIT_APPLY, applyResponses);
    }

    @PostMapping("/periods/{periodId}/applies")
    public ResponseEntity<SuccessResponseBody> saveRecruitApply(@AuthenticationPrincipal UserDetails userDetails,
                                                                @PathVariable Long periodId,
                                                                @RequestBody Map<String, String> recruitApplyRequest) {

        Long recruitApplyId = recruitApplyService.save(periodId, userDetails.getUsername(), recruitApplyRequest);
        return SuccessResponseBody.toResponseEntity(
                StatusEnum.CREATE_RECRUIT_APPLY,
                Map.of("targetPath", "/api/periods/" + periodId + "/applies/" + recruitApplyId)
        );
    }

    @PatchMapping("/periods/{periodId}/applies/{applyId}")
    public ResponseEntity<SuccessResponseBody> updateRecruitApply(@AuthenticationPrincipal UserDetails userDetails,
                                                                @PathVariable Long periodId,
                                                                @PathVariable Long applyId,
                                                                @RequestBody Map<String, String> recruitApplyRequest) {

        Long recruitApplyId = recruitApplyService.update(periodId, applyId, userDetails.getUsername(), recruitApplyRequest);
        return SuccessResponseBody.toResponseEntity(
                StatusEnum.UPDATE_RECRUIT_APPLY,
                Map.of("targetPath", "/api/periods/" + periodId + "/applies/" + recruitApplyId)
        );
    }

    @DeleteMapping("/periods/{periodId}/applies/{applyId}")
    public ResponseEntity<SuccessResponseBody> deleteRecruitApply(@AuthenticationPrincipal UserDetails userDetails,
                                                                  @PathVariable Long periodId,
                                                                  @PathVariable Long applyId) {

         recruitApplyService.delete(periodId, applyId, userDetails.getUsername());
        return SuccessResponseBody.toResponseEntity(StatusEnum.DELETE_RECRUIT_APPLY, null);
    }
}
