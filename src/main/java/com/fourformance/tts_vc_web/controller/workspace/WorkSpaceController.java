package com.fourformance.tts_vc_web.controller.workspace;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.Project;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ErrorResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.workspace.ProjectDto;
import com.fourformance.tts_vc_web.service.workspace.ProjectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workspace")
public class WorkSpaceController {

    private final ProjectService projectService;

    public WorkSpaceController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/project-list")
    public ResponseEntity<?> getRecentProjects(HttpSession session) {
        Long memberId = (Long) session.getAttribute("member_id");

        // Service에서 처리된 응답 그대로 반환
        ResponseDto response = projectService.getRecentProjects(memberId);
        return ResponseEntity.ok(response);
    }
}