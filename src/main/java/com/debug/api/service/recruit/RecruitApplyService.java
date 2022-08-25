package com.debug.api.service.recruit;

import com.debug.api.dto.response.RecruitApplyResponse;
import com.debug.api.entity.recruit.RecruitApply;
import com.debug.api.entity.recruit.RecruitPeriod;
import com.debug.api.entity.user.User;
import com.debug.api.exception.*;
import com.debug.api.repository.recruit.RecruitApplyRepository;
import com.debug.api.repository.recruit.RecruitPeriodRepository;
import com.debug.api.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitApplyService {

    private final RecruitApplyRepository recruitApplyRepository;

    private final RecruitPeriodRepository recruitPeriodRepository;

    private final RecruitPeriodService recruitPeriodService;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RecruitApplyResponse> getAppliesByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        List<RecruitApply> recruitApplies = recruitApplyRepository.getAllByUserOrderByCreatedDateDesc(user);

        return recruitApplies.stream()
                .map(RecruitApplyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long save(Long recruitPeriodId, String userId, Map<String, String> request) {
        RecruitPeriod recruitPeriod = recruitPeriodRepository.findById(recruitPeriodId)
                .orElseThrow(RecruitPeriodNotFoundException::new);

        // todo 2022.08.24 이미 위 코드에서 recruit preiod 정보를 불러와서 불필요한 쿼리 발생, 리팩토링 해야함
        if (!recruitPeriodService.isCanApplyByRecruitPeriodId(recruitPeriodId)) {
            throw new NonReceptionPeriodException();
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if (recruitApplyRepository.existsRecruitApplyByRecruitPeriodAndUser(recruitPeriod, user)) {
            throw new AlreadyAppliedByUserException();
        }

        RecruitApply recruitApply = RecruitApply.builder()
                .user(user)
                .recruitPeriod(recruitPeriod)
                .content(request)
                .isPass(null)
                .build();

        return recruitApplyRepository.save(recruitApply).getId();
    }

    @Transactional
    public Long update(Long recruitPeriodId, Long recruitApplyId, String userId, Map<String, String> request) {
        RecruitApply recruitApply = recruitApplyRepository.findById(recruitApplyId)
                .orElseThrow(RecruitApplyNotFoundException::new);

        if (!recruitApply.getUser().getUserId().equals(userId)) {
            throw new NoRecruitApplyPermissionException();
        }

        // todo 2022.08.24 이미 위 코드에서 recruit preiod 정보를 불러와서 불필요한 쿼리 발생, 리팩토링 해야함
        if (!recruitPeriodService.isCanApplyByRecruitPeriodId(recruitPeriodId)) {
            throw new NonReceptionPeriodException();
        }

        recruitApply.updateContent(request);

        return recruitApplyId;
    }

    @Transactional
    public void delete(Long recruitPeriodId, Long recruitApplyId, String userId) {
        RecruitApply recruitApply = recruitApplyRepository.findById(recruitApplyId)
                .orElseThrow(RecruitApplyNotFoundException::new);

        if (!recruitApply.getUser().getUserId().equals(userId)) {
            throw new NoRecruitApplyPermissionException();
        }

        if (!recruitPeriodService.isCanApplyByRecruitPeriodId(recruitPeriodId)) {
            throw new NonReceptionPeriodException();
        }

        recruitApplyRepository.delete(recruitApply);
    }
}
