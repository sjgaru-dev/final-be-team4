package com.fourformance.tts_vc_web.service.common;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.dto.common.VoiceStyleDto;
import com.fourformance.tts_vc_web.repository.VoiceStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoiceStyleService_team_multi {

    private final VoiceStyleRepository voiceStyleRepository;

    public List<VoiceStyleDto> getVisibleVoiceStyles() {

        try{
            List<VoiceStyle> visibleVoiceStyles = voiceStyleRepository.findVisibleVoiceStyles();

            return visibleVoiceStyles.stream()
                    .map(VoiceStyleDto::createVoiceStyleDto) // ModelMapper를 활용한 변환
                    .collect(Collectors.toList());
        }catch(Exception e){
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
    }
}
