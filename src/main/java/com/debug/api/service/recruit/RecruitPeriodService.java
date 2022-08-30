package com.debug.api.service.recruit;

import com.debug.api.dto.response.RecruitPeriodResponse;
import com.debug.domain.entity.recruit.RecruitPeriod;
import com.debug.common.exception.NonReceptionPeriodException;
import com.debug.common.exception.RecruitPeriodNotFoundException;
import com.debug.api.repository.recruit.RecruitPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RecruitPeriodService {

    private final RecruitPeriodRepository recruitPeriodRepository;

    @Transactional(readOnly = true)
    public RecruitPeriodResponse getRecentRecruitPeriod() {
        RecruitPeriod recentRecruitPeriod = recruitPeriodRepository.getFirstByOrderByCreatedDate()
                .orElseThrow(RecruitPeriodNotFoundException::new);

        if (!isCanApplyByRecentRecruitPeriod()) {
            throw new NonReceptionPeriodException();
        }

        return new RecruitPeriodResponse(recentRecruitPeriod);
    }

    @Transactional(readOnly = true)
    public RecruitPeriodResponse findByRecruitPeriodId(Long id) {
        RecruitPeriod recentRecruitPeriod = recruitPeriodRepository.getFirstByOrderByCreatedDate()
                .orElseThrow(RecruitPeriodNotFoundException::new);

        return new RecruitPeriodResponse(recentRecruitPeriod);
    }

    @Transactional(readOnly = true)
    public List<RecruitPeriodResponse> findAll() {
        List<RecruitPeriod> recruitPeriodList = recruitPeriodRepository.findAllByOrderByCreatedDateDesc();

        return recruitPeriodList.stream()
                .map(RecruitPeriodResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isCanApplyByRecentRecruitPeriod() {
        LocalDateTime now = LocalDateTime.now();
        RecruitPeriod recentRecruitPeriod = recruitPeriodRepository.getFirstByOrderByCreatedDate()
                .orElseThrow(
                        RecruitPeriodNotFoundException::new
                );

        return compareBetweenRecruitPeriod(now, recentRecruitPeriod);
    }

    @Transactional(readOnly = true)
    public boolean isCanApplyByRecruitPeriodId(Long id) {
        LocalDateTime now = LocalDateTime.now();
        RecruitPeriod recentRecruitPeriod = recruitPeriodRepository.findById(id)
                .orElseThrow(
                        RecruitPeriodNotFoundException::new
                );

        return compareBetweenRecruitPeriod(now, recentRecruitPeriod);
    }

    private boolean compareBetweenRecruitPeriod(LocalDateTime timeForCompare, RecruitPeriod recruitPeriod) {
        LocalDateTime startTime = recruitPeriod.getStartDate();
        LocalDateTime endTime = recruitPeriod.getEndDate();

        boolean compareStartDate = timeForCompare.equals(startTime) || timeForCompare.isAfter(startTime);
        boolean compareEndDate = timeForCompare.equals(endTime) || timeForCompare.isBefore(endTime);

        return compareStartDate && compareEndDate;
    }
}
