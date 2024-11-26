package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberIdFindRequestDto {
    /**
     * 회원 ID 찾기 요청 DTO
     * 사용자가 이름과 연락처를 입력해 ID를 찾을 때 사용됩니다.
     */

    private String name; // 회원 이름
    private String phoneNumber; // 회원 연락처
}
