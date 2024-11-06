package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TTSProject extends Project {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id")
    private Style style;

    private String fullScript;
    private Float globalSpeed;
    private Float globalPitch;
    private Float globalVolume;
    private APIStatusConst apiStatus=APIStatusConst.NOT_STARTED;

    // 여러 필드를 동시에 업데이트하는 메서드
    public void updateTTSProject(String projectName, String newFullScript, Float newGlobalSpeed, Float newGlobalPitch, Float newGlobalVolume, APIStatusConst newApiStatus) {
        this.projectName = projectName;
        this.fullScript = newFullScript;
        this.globalSpeed = newGlobalSpeed;
        this.globalPitch = newGlobalPitch;
        this.globalVolume = newGlobalVolume;
        this.setUpdatedAt();
    }

    public void updateAPIStatus(APIStatusConst newApiStatus) {
        this.apiStatus = newApiStatus;
        this.setUpdatedAt();
    }

    // 업데이트 시간을 설정하는 메서드
    private void setUpdatedAt() {
        super.updateUpdatedAt();
    }

    // 생성 메서드
    public static TTSProject createTTSProject(String projectName, Style style, String fullScript, Float globalSpeed, Float globalPitch, Float globalVolume, APIStatusConst apiStatus) {
        TTSProject ttsProject = new TTSProject();
        ttsProject.projectName = projectName;
        ttsProject.fullScript = fullScript;
        ttsProject.setCreatedAt();
        return ttsProject;
    }

    // 생성 시간 설정 메서드
    private void setCreatedAt() {
        super.createCreatedAt();
        super.updateUpdatedAt();
    }
}
