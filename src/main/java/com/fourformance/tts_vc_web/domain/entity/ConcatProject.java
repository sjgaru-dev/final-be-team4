package com.fourformance.tts_vc_web.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcatProject extends Project {
    private Float globalFrontSilenceLength = 0.0F;
    private Float globalTotalSilenceLength = 0.0F;

    // 생성 메서드
    public static ConcatProject createConcatProject() {
        ConcatProject concatProject = new ConcatProject();
        concatProject.setCreatedAt();
        return concatProject;
    }

    // 업데이트 메서드
    public void updateSilenceLengths(Float globalFrontSilenceLength, Float globalTotalSilenceLength) {
        this.globalFrontSilenceLength = globalFrontSilenceLength;
        this.globalTotalSilenceLength = globalTotalSilenceLength;
        setUpdatedAt();
    }

    // 업데이트 시간 설정 메서드
    private void setUpdatedAt() {
        super.updateUpdatedAt();
    }

    // 생성 시간 설정 메서드
    private void setCreatedAt() {
        super.createCreatedAt();
        super.updateUpdatedAt();
    }
}