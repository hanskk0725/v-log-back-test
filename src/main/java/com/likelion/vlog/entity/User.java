package com.likelion.vlog.entity;

import com.likelion.vlog.dto.auth.SignupRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Blog blog;

    private String email;
    private String password;
    private String nickname;

    public static User of(SignupRequestDto signupRequestDto, PasswordEncoder passwordEncoder){
        return new User(signupRequestDto.getEmail(), passwordEncoder.encode(signupRequestDto.getPassword()), signupRequestDto.getNickname());
    }
}
