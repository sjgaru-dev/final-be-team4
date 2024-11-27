package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberIdCheckRequestDto {
    /**
     * 회원 ID 중복체크 요청 DTO
     * 사용자가 입력한 이메일이 중복되는지 확인할 때 사용됩니다.
     */

    private String email; // 중복체크를 하고자 하는 이메일
}
