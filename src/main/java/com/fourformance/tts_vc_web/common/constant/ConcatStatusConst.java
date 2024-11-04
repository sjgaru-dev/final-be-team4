package com.fourformance.tts_vc_web.common.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ConcatStatusConst {
    FAILURE("실패"),
    SUCCESS("성공");

    private final String status;
}
