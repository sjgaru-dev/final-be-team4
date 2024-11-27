package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberPasswordFindRequestDto {
    /**
     * 비밀번호 찾기 요청 DTO
     * 사용자가 이메일과 연락처를 통해 비밀번호를 찾을 때 사용됩니다.
     */

    private String email; // 비밀번호를 찾고자 하는 회원의 이메일
    private String phoneNumber; // 비밀번호를 찾기 위한 회원의 연락처
}
