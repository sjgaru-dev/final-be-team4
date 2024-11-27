package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignUpResponseDto {
    /**
     * 회원가입 응답 DTO
     * 회원가입 완료 후 생성된 정보를 클라이언트로 전달합니다.
     */

    private Long id; // 데이터베이스에 저장된 회원 고유 ID
    private String email; // 회원 이메일
    private String name; // 회원 이름
    private LocalDateTime createdAt; // 회원가입 완료 시간
}
