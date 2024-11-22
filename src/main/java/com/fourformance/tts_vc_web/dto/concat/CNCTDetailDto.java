package com.fourformance.tts_vc_web.dto.concat;

import com.fourformance.tts_vc_web.domain.entity.ConcatDetail;
import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
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
public class CNCTDetailDto {

    private Long id; // ConcatDetail ID
    private Integer audioSeq; // 오디오 순서
    private String srcUrl; // src 오디오 url
    private boolean isChecked; // 체크 상태
    private String unitScript; // 유닛 스크립트
    private Float endSilence; // 끝 부분 침묵 길이

    private static ModelMapper modelMapper = new ModelMapper();

    // CNCTProjectDto -> ConcatProject 매핑 메서드
    public ConcatDetail createConcatDetail() {
        modelMapper.getConfiguration()
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper.map(this, ConcatDetail.class);
    }

    // ConcatProject -> CNCTProjectDto 매핑 메서드
    public static CNCTDetailDto createCNCTDetailDto(ConcatDetail concatDetail) {
        return modelMapper.map(concatDetail, CNCTDetailDto.class);
    }

}
