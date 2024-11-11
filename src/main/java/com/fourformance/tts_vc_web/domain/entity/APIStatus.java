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
@Table(name = "api_status")
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

    // 생성 메서드
    public static APIStatus createAPIStatus(VCDetail vcDetail, TTSDetail ttsDetail,
                                            String requestPayload) {
        APIStatus apiStatus = new APIStatus();
        apiStatus.vcDetail = vcDetail;
        apiStatus.ttsDetail = ttsDetail;
        apiStatus.requestAt = LocalDateTime.now();
        apiStatus.requestPayload = requestPayload;
        return apiStatus;
    }

    // 업데이트 메서드 (응답 받은 시점에 호출되고 응답에 대한 필드가 채워지는 메서드입니다!)
    public void updateResponseInfo(String responsePayload, Integer responseCode, APIUnitStatusConst apiUnitStatusConst) {
        this.apiUnitStatusConst = apiUnitStatusConst;
        this.responseAt = LocalDateTime.now();
        this.responsePayload = responsePayload;
        this.responseCode = responseCode;
    }

}