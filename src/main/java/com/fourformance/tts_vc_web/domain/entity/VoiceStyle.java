package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoiceStyle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voice_style_id")
    private Long id;

    private String country;
    private String languageCode;
    private String voiceType; //"standard"
    private String voiceName;

    private String gender;
    private String personality;
    private boolean isVisible = true;


    // 생성 메서드
    public static VoiceStyle createVoiceStyle(String country, String languageCode, String voiceType,
                                              String voiceName, String gender, String personality) {
        VoiceStyle voiceStyle = new VoiceStyle();
        voiceStyle.country = country;
        voiceStyle.languageCode = languageCode;
        voiceStyle.voiceType = voiceType;
        voiceStyle.voiceName = voiceName;
        voiceStyle.gender = gender;
        voiceStyle.personality = personality;

        return voiceStyle;
    }

    // 업데이트 메서드 - isVisible 상태만 변경할 때 사용
    public void updateVisibility(boolean isVisible) {
        this.isVisible = isVisible;
    }
}