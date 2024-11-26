package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberLoginRequestDto {
    /**
     * 로그인 요청 DTO
     * 사용자가 이메일과 비밀번호를 입력하여 로그인할 때 사용됩니다.
     */

    private String email; // 회원 이메일
    private String pwd; // 회원 비밀번호
}
