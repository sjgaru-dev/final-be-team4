package com.fourformance.tts_vc_web.dto.concat;

import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.vc.VCProjectDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CNCTProjectDto {

    private Long id; // 프로젝트 ID
    private String projectName; // 프로젝트 이름
    private Float globalFrontSilenceLength = 0.0F; // 맨 앞 무음 길이
    private Float globalTotalSilenceLength = 0.0F; // 전체 무음 길이
    private List<ConcatAudioDto> concatAudios;

    private static ModelMapper modelMapper = new ModelMapper();

    // CNCTProjectDto -> ConcatProject 매핑 메서드
    public ConcatProject createConcatProject() {
        modelMapper.getConfiguration()
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper.map(this, ConcatProject.class);
    }

    // ConcatProject -> CNCTProjectDto 매핑 메서드
    public static CNCTProjectDto createCNCTProjectDto(ConcatProject concatProject) {
        return modelMapper.map(concatProject, CNCTProjectDto.class);
    }
}
