package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberIdFindResponseDto {
    /**
     * 회원 ID 찾기 응답 DTO
     * 서버가 찾은 이메일(ID)을 클라이언트로 전달합니다.
     */

    private String email; // 찾은 회원의 이메일
}
