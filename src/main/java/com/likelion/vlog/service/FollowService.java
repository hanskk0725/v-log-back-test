package com.likelion.vlog.service;

import com.likelion.vlog.dto.follows.FollowerGetResponse;
import com.likelion.vlog.dto.follows.FollowingGetResponse;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.repository.FollowRepository;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public Page<FollowingGetResponse> getFollowings(Long userId, Pageable pageable) {
        User user = getUser(userId);

        return followRepository.findByFollower(user, pageable)
                .map(follow ->
                        FollowingGetResponse.of(
                                follow.getFollowing(),
                                true
                        )
                );
    }

    public Page<FollowerGetResponse> getFollowers(Long userId, Pageable pageable) {
        User user = getUser(userId);

        return followRepository.findByFollowing(user, pageable)
                .map(follow -> {
                    User follower = follow.getFollower();
                    boolean isFollowing =
                            followRepository.existsByFollowerAndFollowing(user, follower);

                    return FollowerGetResponse.of(follower, isFollowing);
                });
    }

    // private User getUser(Long userId) {
    //     return userRepository.findById(userId)
    //             .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    // }
    private User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 로그 추가
        System.out.println("Found user: " + user.getNickname() + " (id=" + user.getId() + ")");

        return user;
    }

}
