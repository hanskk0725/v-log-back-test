package com.likelion.vlog.service;

import com.likelion.vlog.dto.user.UserDto;
import com.likelion.vlog.dto.user.UserUpdateRequestDto;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV2 {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));
        return UserDto.of(user);
    }

    @Transactional
    public UserDto updateUser(Long userId, UserUpdateRequestDto request, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        // 권한 검증: 본인만 수정 가능
        if (!user.getEmail().equals(email)) {
            throw ForbiddenException.userUpdate();
        }

        user.upDateInfo(request, passwordEncoder);
        return UserDto.of(user);
    }

    @Transactional
    public void deleteUser(Long userId, String password, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        // 권한 검증: 본인만 탈퇴 가능
        if (!user.getEmail().equals(email)) {
            throw ForbiddenException.userDelete();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(user);
    }
}
