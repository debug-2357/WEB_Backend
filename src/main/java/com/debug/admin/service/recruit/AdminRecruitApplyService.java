package com.debug.admin.service.recruit;

import com.debug.admin.dto.request.AdminIsPassType;
import com.debug.admin.repository.recruit.AdminRecruitApplyRepository;
import com.debug.common.exception.RecruitApplyNotFoundException;
import com.debug.domain.entity.recruit.RecruitApply;
import com.debug.domain.entity.recruit.RecruitPeriod;
import com.debug.domain.entity.user.User;
import com.debug.oauth.entity.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRecruitApplyService {

    private final AdminRecruitPeriodService adminRecruitPeriodService;
    private final AdminRecruitApplyRepository adminRecruitApplyRepository;

    @Transactional(readOnly = true)
    public RecruitApply findById(Long recruitApplyId) {
        return adminRecruitApplyRepository.findById(recruitApplyId)
                .orElseThrow(RecruitApplyNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Page<RecruitApply> findAllByRecruitPeriod(Long recruitPeriodId, Pageable pageable) {
        RecruitPeriod recruitPeriod = adminRecruitPeriodService.findById(recruitPeriodId);
        return adminRecruitApplyRepository.findAllByRecruitPeriod(recruitPeriod, pageable);
    }

    @Transactional
    public RecruitApply updateIsPassById(Long recruitApplyId, String isPassString) {
        RecruitApply recruitApply = findById(recruitApplyId);
        User applyUser = recruitApply.getUser();
        AdminIsPassType isPassType = AdminIsPassType.of(isPassString);

        if (isPassType.getIsPass()) {
            applyUser.updateRoleType(RoleType.CONFIRM);
        } else {
            applyUser.updateRoleType(RoleType.UNCONFIRMED);
        }

        recruitApply.updateIsPass(isPassType.getIsPass());
        return recruitApply;
    }
}
