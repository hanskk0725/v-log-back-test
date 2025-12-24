package com.likelion.vlog.dto.user;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class UserFollowDTO { //팔로우, 팔로잉 표시
    private Long userId;
    private String userNickname;
    private List<FollowUserDTO> follows;

    @Getter
    public static class FollowUserDTO {
        private Long id;
        private String nickname;
    }
}

