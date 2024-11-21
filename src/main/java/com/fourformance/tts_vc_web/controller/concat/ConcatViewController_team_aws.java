package com.fourformance.tts_vc_web.controller.concat;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.concat.ConcatSaveDto;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.service.common.ProjectService_team_aws;
import com.fourformance.tts_vc_web.service.concat.ConcatService_team_aws;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/concat")
public class ConcatViewController_team_aws {

    private final ConcatService_team_aws concatService;
    private final ProjectService_team_aws projectService;

    // Concat 상태 저장 메서드
    @Operation(
            summary = "Concat 상태 저장",
            description = "Concat 프로젝트 상태를 저장합니다.")
    @PostMapping("/save")
    public ResponseDto concatSave(@RequestBody ConcatSaveDto concatSaveDto,
                                  HttpSession session) {
        try {

//            Long memberId = (Long) session.getAttribute("memberId");
            Long memberId = 1L; // 개발단계 임시 하드코딩

            Long projectId;
            if (concatSaveDto.getProjectId() == null) {
                // projectId가 null인 경우, 새 프로젝트 생성
                projectId = concatService.createNewProject(concatSaveDto, memberId);
            } else {
                // projectId가 존재하면, 기존 프로젝트 업데이트
                projectId = concatService.updateProject(concatSaveDto, memberId);
            }
            return DataResponseDto.of(projectId, "상태가 성공적으로 저장되었습니다.");
        } catch (BusinessException e) {
            throw e;
        }
    }

    // Concat 프로젝트 삭제
    @Operation(
            summary = "Concat 선택된 항목 삭제",
            description = "Concat 프로젝트에서 선택된 모든 항목을 삭제합니다.")
    @PostMapping("/delete/{projectId}")
    public ResponseDto deleteConcatProject(@PathVariable("projectId") Long projectId) {

        // 타입 검증
        if (projectId == null) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_ID);
        }

        // 프로젝트 삭제
        projectService.deleteProject(projectId);

        // 작업 상태 : Terminated (종료)
        return DataResponseDto.of("", "Concat 프로젝트가 정상적으로 삭제되었습니다.");
    }

    // Concat 선택된 모든 유닛 삭제
    @Operation(
            summary = "Concat 선택된 항목 삭제",
            description = "Concat 프로젝트에서 선택된 모든 항목을 삭제합니다.")
    @PostMapping("/delete/details")
    public ResponseDto deleteConcatDetails(@RequestBody List<Long> concatDetailsId) {

        // 선택 항목 삭제
        projectService.deleteSelectedDetails(concatDetailsId);

        return DataResponseDto.of("", "선택된 모든 항목이 정상적으로 삭제되었습니다.");
    }
}
