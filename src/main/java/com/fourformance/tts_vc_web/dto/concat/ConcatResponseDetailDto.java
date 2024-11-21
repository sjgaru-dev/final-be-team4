package com.fourformance.tts_vc_web.dto.concat;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConcatResponseDetailDto {

    private Long id;
    private Integer audioSeq;
    private boolean isChecked;
    private String unitScript;
    private Float endSilence;
    private MultipartFile sourceAudio;
    private String audioUrl;

}
