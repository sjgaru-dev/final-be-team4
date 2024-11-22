package com.fourformance.tts_vc_web.dto.common;

import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoiceStyleDto {

    private Long id;             // VoiceStyle ID
    private String country;      // 국가
    private String languageCode; // 언어 코드
    private String voiceType;    // 음성 타입 ("standard", "wavenet" 등)
    private String voiceName;    // 음성 이름
    private String gender;       // 성별
    private String personality;  // 성격 설명

    private static ModelMapper modelMapper = new ModelMapper();

    public VoiceStyle createVoiceStyle(){
        modelMapper.getConfiguration()
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper.map(this, VoiceStyle.class);
    }

    public static VoiceStyleDto createVoiceStyleDto(VoiceStyle voiceStyle) {
        return modelMapper.map(voiceStyle, VoiceStyleDto.class);
    }

}
