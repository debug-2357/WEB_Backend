package com.debug.admin.repository.recruit;

import com.debug.domain.entity.recruit.RecruitApply;
import com.debug.domain.entity.recruit.RecruitPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AdminRecruitApplyRepository extends PagingAndSortingRepository<RecruitApply, Long> {
    Page<RecruitApply> findAllByRecruitPeriod(RecruitPeriod recruitPeriod, Pageable pageable);
}