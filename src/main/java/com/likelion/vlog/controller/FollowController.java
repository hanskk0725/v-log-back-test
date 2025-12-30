package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.follows.PageResponse;
import com.likelion.vlog.dto.follows.FollowerGetResponse;
import com.likelion.vlog.dto.follows.FollowingGetResponse;
import com.likelion.vlog.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 팔로워/팔로잉 목록 조회 API 컨트롤러
 * - base URL : /api/v1/users
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로워 목록 조회
     */
    @GetMapping("/{user_id}/followers")
    public ResponseEntity<ApiResponse<PageResponse<FollowerGetResponse>>> getFollowers(@PathVariable("user_id") Long userId, Pageable pageable) {
        Page<FollowerGetResponse> page = followService.getFollowers(userId, pageable);
        PageResponse<FollowerGetResponse> response = new PageResponse<>(
                page.getContent(),
                new PageResponse.PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success("팔로워 목록 조회 성공", response)
        );
    }

    /**
     * 팔로잉 목록 조회
     */
    @GetMapping("/{user_id}/followings")
    public ResponseEntity<ApiResponse<PageResponse<FollowingGetResponse>>> getFollowings(@PathVariable("user_id") Long userId, Pageable pageable) {
        Page<FollowingGetResponse> page = followService.getFollowings(userId, pageable);
        PageResponse<FollowingGetResponse> response = new PageResponse<>(
                page.getContent(),
                new PageResponse.PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success("팔로잉 목록 조회 성공", response)
        );
    }
}
