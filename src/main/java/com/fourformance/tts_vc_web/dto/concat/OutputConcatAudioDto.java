package com.fourformance.tts_vc_web.dto.concat;

import com.fourformance.tts_vc_web.common.constant.AudioFormat;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutputConcatAudioDto {


    private Long generatedAudioMetaId;
    private Long concatProjectId;
    private ProjectType projectType;
    private String audioUrl;
    private AudioFormat audioFormat;


}
