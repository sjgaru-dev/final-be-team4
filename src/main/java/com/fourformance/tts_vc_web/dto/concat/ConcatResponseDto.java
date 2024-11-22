package com.fourformance.tts_vc_web.dto.concat;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConcatResponseDto {

    private Long projectId;
    private String projectName;
    private Float globalFrontSilenceLength;
    private Float globalTotalSilenceLength;

    private List<String> outputConcatAudios;
    private List<ConcatResponseDetailDto> concatResponseDetails;

}
