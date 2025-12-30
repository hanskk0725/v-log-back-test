package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Follow;
import com.likelion.vlog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Page<Follow> findByFollower(User follower, Pageable pageable);

    Page<Follow> findByFollowing(User following, Pageable pageable);

    boolean existsByFollowerAndFollowing(User follower, User following);
}
