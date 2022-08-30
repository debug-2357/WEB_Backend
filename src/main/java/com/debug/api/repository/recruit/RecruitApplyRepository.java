package com.debug.api.repository.recruit;

import com.debug.domain.entity.recruit.RecruitApply;
import com.debug.domain.entity.recruit.RecruitPeriod;
import com.debug.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecruitApplyRepository extends JpaRepository<RecruitApply, Long> {
    List<RecruitApply> getAllByUserOrderByCreatedDateDesc(User user);

    Optional<RecruitApply> getByRecruitPeriodAndUser(RecruitPeriod recruitPeriod, User user);

    boolean existsRecruitApplyByRecruitPeriodAndUser(RecruitPeriod recruitPeriod, User user);
}