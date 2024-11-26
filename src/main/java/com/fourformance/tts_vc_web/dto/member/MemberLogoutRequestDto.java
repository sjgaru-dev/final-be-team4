package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberLogoutRequestDto {
    /**
     * 로그아웃 요청 DTO
     * 클라이언트가 특정 회원의 로그아웃 요청을 서버로 보낼 때 사용됩니다.
     */

    private Long memberId; // 로그아웃할 회원의 고유 ID
}
