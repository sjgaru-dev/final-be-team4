package com.fourformance.tts_vc_web.repository.workspace;

import com.fourformance.tts_vc_web.dto.workspace.ProjectListDto;
import java.util.List;

public interface ProjectRepositoryCustom {
    List<ProjectListDto> findProjectsBySearchCriteria(Long memberId, String keyword);
}
