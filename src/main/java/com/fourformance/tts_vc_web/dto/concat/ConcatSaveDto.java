package com.fourformance.tts_vc_web.dto.concat;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConcatSaveDto {

    private Long projectId;
    private String projectName;
    private Float globalFrontSilenceLength;
    private Float globalTotalSilenceLength;
    private List<ConcatDetailDto> concatDetails;
}
