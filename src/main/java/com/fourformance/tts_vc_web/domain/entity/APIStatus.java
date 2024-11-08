package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class APIStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vc_detail_id")
    private VCDetail vcDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tts_detail_id")
    private TTSDetail ttsDetail;

    @Enumerated(EnumType.STRING)
    private APIUnitStatusConst apiUnitStatusConst;
    private LocalDateTime requestAt;
    private LocalDateTime responseAt;
    private String requestPayload;
    private String responsePayload;
    private Integer responseCode;

    // 응답 정보 업데이트 메서드
    public void updateResponseInfo(String responsePayload, Integer responseCode) {
        this.responseAt = LocalDateTime.now();
        this.responsePayload = responsePayload;
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
        apiStatus.requestPayload = requestPayload;
        return apiStatus;
    }
}