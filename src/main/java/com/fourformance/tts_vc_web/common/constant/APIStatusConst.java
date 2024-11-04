package com.fourformance.tts_vc_web.common.constant;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum APIStatusConst {
    IN_PROGRESS("진행중"),
    SUCCESS("성공"),
    FAILURE("완전 실패"),
    PARTIAL_FAILURE("부분 실패"),
    CANCELLED("중도 취소"),
    NOT_STARTED("시작 전");

    private final String status;

}
