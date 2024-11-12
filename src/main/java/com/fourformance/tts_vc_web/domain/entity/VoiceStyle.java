package com.fourformance.tts_vc_web.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoiceStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "style_id")
    private Long id;

    private String country;
    private String languageCode;
    private String voiceType;
    private String voiceName;
    private String gender;
    private String personality;
    private boolean isVisible = true;

    public VoiceStyle(String country, String languageCode, String voiceType, String voiceName, String gender, String personality) {
        this.country = country;
        this.languageCode = languageCode;
        this.voiceType = voiceType;
        this.voiceName = voiceName;
        this.gender = gender;
        this.personality = personality;
    }

    // 생성 메서드
    public static VoiceStyle createVoiceStyle(String country, String languageCode, String voiceType,
                                              String voiceName, String gender, String personality) {
        return new VoiceStyle(country, languageCode, voiceType, voiceName, gender, personality);
    }

    // 업데이트 메서드 - 가시성 상태만 변경할 때 사용
    public void updateVisibility(boolean isVisible) {
        this.isVisible = isVisible;
    }
}