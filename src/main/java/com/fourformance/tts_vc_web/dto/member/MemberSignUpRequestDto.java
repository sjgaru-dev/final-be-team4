package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignUpRequestDto {
    /**
     * 회원가입 요청 DTO
     * 사용자가 입력한 회원 정보를 담아 서버로 전달합니다.
     */

    private String email; // 회원 이메일 (유니크한 ID로 사용)
    private String pwd; // 회원 비밀번호 (평문으로 전달되며 서버에서 암호화 처리 필요)
    private String name; // 회원 이름
    private Integer gender; // 성별 (1: 남성, 2: 여성 등으로 정의 가능)
    private Date birthDate; // 생년월일
    private String phoneNumber; // 연락처
}
