package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VCProjectDto {

    private Long id; // 프로젝트 ID
    private String projectName; // 프로젝트 이름
    private APIStatusConst apiStatus; // API 상태
    private Long memberTargetAudioMetaId; // MemberAudioMeta ID
    private LocalDateTime apiStatusModifiedAt; // API 상태 수정일
    private Boolean isDeleted; // 삭제 여부

    private static ModelMapper modelMapper = new ModelMapper();

    // VCProjectDto -> VCProject 매핑 메서드
    public VCProject createVCProject() {
        modelMapper.getConfiguration()
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper.map(this, VCProject.class);
    }

    // VCProject -> VCProjectDto 매핑 메서드
    public static VCProjectDto createVCProjectDto(VCProject vcProject) {
        return modelMapper.map(vcProject, VCProjectDto.class);
    }
}
