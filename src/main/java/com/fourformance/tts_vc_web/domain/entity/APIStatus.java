package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class APIStatus {

    @Id
    @GeneratedValue
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vc_detail_id")
    private VCDetail vcDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tts_detail_id")
    private TTSDetail ttsDetail;

    private APIUnitStatusConst apiUnitStatusConst;
    private LocalDateTime requestAt;
    private LocalDateTime responseAt;
    private String requestPaylaod;
    private String responsePaylaod;
    private Integer responseCode;

    // 응답 정보 업데이트 메서드
    public void updateResponseInfo(String responsePayload, Integer responseCode) {
        this.responseAt = LocalDateTime.now();
        this.responsePaylaod = responsePayload;
        this.responseCode = responseCode;
    }

    public static APIStatus createAPIStatus(VCDetail vcDetail, TTSDetail ttsDetail,
                                            APIUnitStatusConst apiUnitStatusConst,
                                            String requestPayload) {
        APIStatus apiStatus = new APIStatus();
        apiStatus.vcDetail = vcDetail;
        apiStatus.ttsDetail = ttsDetail;
        apiStatus.apiUnitStatusConst = apiUnitStatusConst;
        apiStatus.requestAt = LocalDateTime.now();
        apiStatus.requestPaylaod = requestPayload;
        return apiStatus;
    }
}