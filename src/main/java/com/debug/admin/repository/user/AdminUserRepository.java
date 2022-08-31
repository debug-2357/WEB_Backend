package com.debug.admin.repository.user;

import com.debug.domain.entity.user.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AdminUserRepository extends PagingAndSortingRepository<User, Long> {
}