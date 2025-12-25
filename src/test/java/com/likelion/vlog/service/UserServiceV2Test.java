package com.likelion.vlog.service;

import com.likelion.vlog.dto.user.UserDto;
import com.likelion.vlog.dto.user.UserUpdateRequestDto;
import com.likelion.vlog.entity.Blog;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceV2Test {

    @InjectMocks
    private UserServiceV2 userServiceV2;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = createTestUser(1L, "test@test.com", "encodedPassword", "테스터");
    }

    @Nested
    @DisplayName("사용자 조회")
    class GetUser {

        @Test
        @DisplayName("사용자 조회 성공")
        void getUser_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserDto result = userServiceV2.getUser(1L);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("test@test.com");
            assertThat(result.getNickname()).isEqualTo("테스터");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void getUser_NotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userServiceV2.getUser(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정")
    class UpdateUser {

        @Test
        @DisplayName("본인 정보 수정 성공")
        void updateUser_Success() {
            // given
            UserUpdateRequestDto request = new UserUpdateRequestDto();
            ReflectionTestUtils.setField(request, "nickname", "새닉네임");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserDto result = userServiceV2.updateUser(1L, request, "test@test.com");

            // then
            assertThat(result.getNickname()).isEqualTo("새닉네임");
        }

        @Test
        @DisplayName("다른 사용자 정보 수정 시 403 예외")
        void updateUser_Forbidden() {
            // given
            UserUpdateRequestDto request = new UserUpdateRequestDto();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userServiceV2.updateUser(1L, request, "other@test.com"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("수정 권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 수정 시 404 예외")
        void updateUser_NotFound() {
            // given
            UserUpdateRequestDto request = new UserUpdateRequestDto();
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userServiceV2.updateUser(999L, request, "test@test.com"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class DeleteUser {

        @Test
        @DisplayName("본인 탈퇴 성공")
        void deleteUser_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when
            userServiceV2.deleteUser(1L, "password123", "test@test.com");

            // then
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("다른 사용자 탈퇴 시도 시 403 예외")
        void deleteUser_Forbidden() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userServiceV2.deleteUser(1L, "password123", "other@test.com"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("탈퇴 권한이 없습니다");
        }

        @Test
        @DisplayName("비밀번호 불일치 시 예외 발생")
        void deleteUser_WrongPassword() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userServiceV2.deleteUser(1L, "wrongPassword", "test@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호가 일치하지 않습니다");
        }
    }

    // 헬퍼 메서드
    private User createTestUser(Long id, String email, String password, String nickname) {
        try {
            java.lang.reflect.Constructor<User> constructor = User.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            User user = constructor.newInstance();
            ReflectionTestUtils.setField(user, "id", id);
            ReflectionTestUtils.setField(user, "email", email);
            ReflectionTestUtils.setField(user, "password", password);
            ReflectionTestUtils.setField(user, "nickname", nickname);

            // Blog 설정 (UserDto.of()에서 필요)
            java.lang.reflect.Constructor<Blog> blogConstructor = Blog.class.getDeclaredConstructor();
            blogConstructor.setAccessible(true);
            Blog blog = blogConstructor.newInstance();
            ReflectionTestUtils.setField(blog, "id", 1L);
            ReflectionTestUtils.setField(blog, "title", nickname + "의 블로그");
            ReflectionTestUtils.setField(user, "blog", blog);

            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
