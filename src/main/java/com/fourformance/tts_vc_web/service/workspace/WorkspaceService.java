package com.fourformance.tts_vc_web.service.workspace;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.Project;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.workspace.RecentProjectDto;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final ProjectRepository projectRepository;

    public List<RecentProjectDto> getRecentProjects(Long memberId) {
        // memberId가 null이면 예외 발생
        if (memberId == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // DB에서 최신 5개의 프로젝트 조회
        List<Project> projects = projectRepository.findTop5ByMemberIdOrderByUpdatedAtDesc(memberId);

        // 프로젝트 리스트를 DTO로 변환
        return projects.stream().map(project -> {
            String type = convertProjectType(project); // 프로젝트 타입 결정

            APIStatusConst apiStatus = null;

            // API 상태를 TTSProject와 VCProject에 따라 가져옴
            if (project instanceof TTSProject) {
                apiStatus = ((TTSProject) project).getApiStatus();
            } else if (project instanceof VCProject) {
                apiStatus = ((VCProject) project).getApiStatus();
            }

            return new RecentProjectDto(
                    project.getId(),
                    type,
                    project.getProjectName(),
                    apiStatus, // 상태는 TTSProject와 VCProject만 포함, 나머지는 null
                    project.getCreatedAt(),
                    project.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }

    private String convertProjectType(Project project) {
        // 프로젝트의 클래스 이름을 기반으로 적절한 타입 문자열로 변환
        String simpleName = project.getClass().getSimpleName();
        switch (simpleName) {
            case "TTSProject":
                return "TTS";
            case "VCProject":
                return "VC";
            case "ConcatProject":
                return "Concat";
            default:
                throw new BusinessException(ErrorCode.UNSUPPORTED_PROJECT_TYPE); // 지원하지 않는 타입 처리
        }
    }
}