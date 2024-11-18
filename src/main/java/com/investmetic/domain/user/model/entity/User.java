package com.investmetic.domain.user.model.entity;

import com.investmetic.domain.user.model.Role;
import com.investmetic.domain.user.model.UserState;
import com.investmetic.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // 회원 ID, 기본 키로 자동 증가됨

    @Column(name="user_name")
    private String userName; // 사용자 이름 (로그인 아이디로 사용될 수 있음)

    private String nickname; // 사용자 닉네임 (표시 이름)

    private String email; // 이메일 주소

    private String password; // 비밀번호 (암호화 필요)

    @Column(length = 1000)
    private String imageUrl; // 프로필 이미지 URL

    private String phone; // 전화번호

    private String birthDate; // 생년월일 (YYYYMMDD 형식)

    private String ipAddress; // 마지막 로그인 시 사용한 IP 주소

    private Boolean infoAgreement; // 정보 제공 동의 여부 (true: 동의, false: 비동의)

    private LocalDate joinDate; // 가입일자

    private LocalDate withdrawalDate; // 탈퇴일자 (탈퇴한 경우에만 값이 있음)

    @Enumerated(EnumType.STRING)
    private UserState userState; // 회원 상태

    private Boolean withdrawalStatus; // 탈퇴 여부

    @Enumerated(EnumType.STRING)
    private Role role; // 회원 등급 또는 역할

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade=CascadeType.ALL ,orphanRemoval = true)
     List<UserHistory> userHistory; //회원 변경 이력 (user Entity만 가지고 있음)

    @Builder
    public User(String userName, String nickname, String email, String password, String imageUrl,
                String phone,
                String birthDate, String ipAddress, Boolean infoAgreement, LocalDate joinDate, LocalDate withdrawalDate,
                UserState userState, Boolean withdrawalStatus, Role role) {

        this.userName = userName;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.imageUrl = imageUrl;
        this.phone = phone;
        this.birthDate = birthDate;
        this.ipAddress = ipAddress;
        this.infoAgreement = infoAgreement;
        this.joinDate = joinDate;
        this.withdrawalDate = withdrawalDate;
        this.userState = userState;
        this.withdrawalStatus = withdrawalStatus;
        this.role = role;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void addUserHistory(UserHistory userHistory){
        if (this.userHistory == null) {
            this.userHistory = new ArrayList<>();
        }
        this.userHistory.add(userHistory);
    }

}

