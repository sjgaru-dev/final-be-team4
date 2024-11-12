package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tts_project")
public class TTSProject extends Project {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id")
    private Style style;

    private String fullScript;

    private Float globalSpeed;
    private Float globalPitch;
    private Float globalVolume;

    @Enumerated(EnumType.STRING)
    private APIStatusConst apiStatus = APIStatusConst.NOT_STARTED;
    private LocalDateTime APIStatusModifiedAt;


    // 생성 메서드
    public static TTSProject createTTSProject(Member member, String projectName, String fullScript, Float globalSpeed, Float globalPitch, Float globalVolume) {
        TTSProject ttsProject = new TTSProject();
        ttsProject.member = member;
        ttsProject.projectName = projectName;
        ttsProject.fullScript = fullScript;
        ttsProject.globalSpeed = globalSpeed;
        ttsProject.globalPitch = globalPitch;
        ttsProject.globalVolume = globalVolume;
        ttsProject.createdAt();
        ttsProject.updatedAt();
        return ttsProject;
    }

    // 업데이트 메서드
    public void updateTTSProject(String projectName, String fullScript, Float globalSpeed, Float globalPitch, Float globalVolume) {
        super.projectName = projectName;
        this.fullScript = fullScript;
        this.globalSpeed = globalSpeed;
        this.globalPitch = globalPitch;
        this.globalVolume = globalVolume;
        super.updatedAt();
    }

    // API 상태 변경 메서드
    public void updateAPIStatus(APIStatusConst newApiStatus) {
        this.apiStatus = newApiStatus;
        this.APIStatusModifiedAt = LocalDateTime.now();
    }



}
