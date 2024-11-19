//package com.fourformance.tts_vc_web.service.workspace;
//
//import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
//import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
//import com.fourformance.tts_vc_web.domain.entity.Project;
//import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
//import com.fourformance.tts_vc_web.dto.response.ResponseDto;
//import com.fourformance.tts_vc_web.dto.workspace.RecentProjectDto;
//import com.fourformance.tts_vc_web.repository.ProjectRepository;
//import java.util.ArrayList;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class ProjectService {
//
//    private final ProjectRepository projectRepository;
//
//    public ResponseDto getRecentProjects(Long memberId) {
//        // memberId가 null이면 ErrorResponseDto 반환
//        if (memberId == null) {
//            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
//        }
//
//        List<Project> projects = projectRepository.findTop5ByMemberIdOrderByCreatedAtDesc(memberId);
//
//        List<RecentProjectDto> projectDtoList = new ArrayList<>();
//        for (Project project : projects) {
//            String type = project.getClass().getSimpleName(); // 클래스 이름으로 타입 설정
//            projectDtoList.add(new RecentProjectDto(
//                    project.getId(),
//                    "/projects/" + project.getId(),
//                    project.getProjectName(),
//                    type, // DTO에 타입 추가
//                    project.getCreatedAt(),
//                    project.getUpdatedAt()
//            ));
//        }
//        return DataResponseDto.of(projectDtoList); // 리스트 반환
//    }
//}