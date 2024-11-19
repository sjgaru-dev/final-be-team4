package com.fourformance.tts_vc_web.service.workspace;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.APIStatus;
import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.Project;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.workspace.RecentExportDto;
import com.fourformance.tts_vc_web.dto.workspace.RecentProjectDto;
import com.fourformance.tts_vc_web.repository.OutputAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final ProjectRepository projectRepository;
    private final OutputAudioMetaRepository outputAudioMetaRepository;

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

    // -------------

    public List<RecentExportDto> getRecentAudioExports() {
        // 최근 생성된 5개의 OutputAudioMeta 조회
        List<OutputAudioMeta> recentAudios = outputAudioMetaRepository.findTop5RecentOutputAudioMeta();

        // OutputAudioMeta 리스트를 RecentExportDto로 변환
        return recentAudios.stream().map(outputAudioMeta -> {
            String projectName = null;
            String script = null;
            APIStatus unitStatus = null;

            // TTSDetail 데이터 처리
            if (outputAudioMeta.getTtsDetail() != null) {
                TTSDetail ttsDetail = outputAudioMeta.getTtsDetail();
                projectName = ttsDetail.getTtsProject().getProjectName();
                script = ttsDetail.getUnitScript();
                unitStatus = ttsDetail.getApiStatuses().get(ttsDetail.getApiStatuses().size() - 1); // TTS 유닛별 상태 가져오기
            }
            // VCDetail 데이터 처리
            else if (outputAudioMeta.getVcDetail() != null) {
                VCDetail vcDetail = outputAudioMeta.getVcDetail();
                projectName = vcDetail.getProject().getProjectName();
                script = vcDetail.getUnitScript();
                unitStatus = vcDetail.getApiStatus(); // VC 유닛별 상태 가져오기
            }
            // ConcatProject 데이터 처리
            else if (outputAudioMeta.getConcatProject() != null) {
                projectName = outputAudioMeta.getConcatProject().getProjectName();
                script = null; // Concat 프로젝트는 유닛 스크립트가 없음
                unitStatus = null; // Concat 프로젝트는 API 상태 없음
            }

            return new RecentExportDto(
                    outputAudioMeta.getId(),
                    projectName,
                    script,
                    extractFileName(outputAudioMeta.getAudioUrl()), // URL에서 파일 이름 추출
                    outputAudioMeta.getAudioUrl(),
                    unitStatus,
                    outputAudioMeta.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    // Utility 메서드: URL에서 파일 이름 추출
    private String extractFileName(String audioUrl) {
        if (audioUrl == null || !audioUrl.contains("/")) {
            return null;
        }
        return audioUrl.substring(audioUrl.lastIndexOf('/') + 1);
    }

}