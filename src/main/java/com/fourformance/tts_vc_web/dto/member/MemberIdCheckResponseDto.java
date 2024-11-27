package com.fourformance.tts_vc_web.dto.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberIdCheckResponseDto {
    /**
     * 회원 ID 중복체크 응답 DTO
     * 서버가 중복 여부를 확인한 결과를 클라이언트로 전달합니다.
     */

    private Boolean isDuplicate; // 이메일이 중복되었는지 여부 (true: 중복, false: 사용 가능)
}
