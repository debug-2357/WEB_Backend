package com.debug.admin.service.recruit;

import com.debug.admin.dto.request.AdminRecruitPeriodRequest;
import com.debug.admin.repository.recruit.AdminRecruitPeriodRepository;
import com.debug.common.exception.RecruitPeriodNotFoundException;
import com.debug.domain.entity.recruit.RecruitPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRecruitPeriodService {

    private final AdminRecruitPeriodRepository adminRecruitPeriodRepository;

    @Transactional
    public RecruitPeriod save(AdminRecruitPeriodRequest recruitPeriodRequest) {
        RecruitPeriod recruitPeriod = RecruitPeriod.builder()
                .yearOf(recruitPeriodRequest.getYearOf())
                .startDate(recruitPeriodRequest.getStartDate())
                .endDate(recruitPeriodRequest.getEndDate())
                .questions(recruitPeriodRequest.getQuestions())
                .build();

        return adminRecruitPeriodRepository.save(recruitPeriod);
    }

    @Transactional(readOnly = true)
    public RecruitPeriod findById(Long recruitPeriodId) {
        return adminRecruitPeriodRepository.findById(recruitPeriodId)
                .orElseThrow(RecruitPeriodNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Page<RecruitPeriod> findAll(Pageable pageable) {
        return adminRecruitPeriodRepository.findAll(pageable);
    }

    @Transactional
    public RecruitPeriod updateById(Long recruitPeriodId, AdminRecruitPeriodRequest recruitPeriodRequest) {
        RecruitPeriod recruitPeriod = findById(recruitPeriodId);
        recruitPeriod.update(recruitPeriodRequest);

        return recruitPeriod;
    }

    @Transactional
    public void deleteById(Long recruitPeriodId) {
        adminRecruitPeriodRepository.deleteById(recruitPeriodId);
    }
}
