package com.fourformance.tts_vc_web.dto.concat;

import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConcatProjectDto {

    private Long id;
    private String projectName;
    private Float globalFrontSilenceLength;
    private Float globalTotalSilenceLength;

    public static ConcatProjectDto createFromEntity(ConcatProject concatProject) {
        return new ConcatProjectDto(
                concatProject.getId(),
                concatProject.getProjectName(),
                concatProject.getGlobalFrontSilenceLength(),
                concatProject.getGlobalTotalSilenceLength()
        );
    }
}
