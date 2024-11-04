package com.fourformance.tts_vc_web.common.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MultiJobLogStatusConst {
    NEW("..."),
    RUNNABLE("실행중"),
    BLOCKED("일시정지"),
    WAITING("일시정지"),
    TERMINATED("종료");

    private final String descriptions;
}
