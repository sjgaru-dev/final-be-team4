package com.fourformance.tts_vc_web.common.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum APIUnitStatusConst {
    SUCCESS("성공"),
    FAILURE("실패");

    private final String value;
}
