package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    public static APIStatus createAPIStatus(VCDetail vcDetail, TTSDetail ttsDetail, String requestPayload) {
        APIStatus apiStatus = new APIStatus();
        apiStatus.vcDetail = vcDetail;
        apiStatus.ttsDetail = ttsDetail;
        apiStatus.requestAt = LocalDateTime.now();
        apiStatus.requestPayload = requestPayload;

        // 연관관계 편의 메서드 호출
        if (vcDetail != null) {
            vcDetail.addAPIStatus(apiStatus);
        }
        if (ttsDetail != null) {
            ttsDetail.addAPIStatus(apiStatus);
        }

        return apiStatus;
    }

    // 업데이트 메서드
    public void updateResponseInfo(String responsePayload, Integer responseCode,
                                   APIUnitStatusConst apiUnitStatusConst) {
        this.apiUnitStatusConst = apiUnitStatusConst;
        this.responseAt = LocalDateTime.now();
        this.responsePayload = responsePayload;
        this.responseCode = responseCode;
    }

    // TTSDetail와 VCDetail setter
    public void setTtsDetail(TTSDetail ttsDetail) {
        this.ttsDetail = ttsDetail;
    }

    public void setVcDetail(VCDetail vcDetail) {
        this.vcDetail = vcDetail;
    }
}