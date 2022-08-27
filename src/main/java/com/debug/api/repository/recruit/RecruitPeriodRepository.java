package com.debug.api.repository.recruit;

import com.debug.api.entity.recruit.RecruitPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecruitPeriodRepository extends JpaRepository<RecruitPeriod, Long> {

    List<RecruitPeriod> findAllByOrderByCreatedDateDesc();
    Optional<RecruitPeriod> getFirstByOrderByCreatedDate();
}