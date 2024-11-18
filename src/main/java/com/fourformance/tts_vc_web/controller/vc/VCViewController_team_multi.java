package com.fourformance.tts_vc_web.controller.vc;

import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
import com.fourformance.tts_vc_web.service.vc.VCService_team_multi;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vc")
@RequiredArgsConstructor
public class VCViewController_team_multi {

    @Autowired
    VCService_team_multi vcService;

    // VC 상태 로드 메서드
    @Operation(
            summary = "VC 상태 로드",
            description = "VC 프로젝트 상태를 가져옵니다." )
    @GetMapping("/{projectId}")
    public ResponseDto ttsLoad(@PathVariable Long projectId) {

        return DataResponseDto.of("");
    }

    // VC 상태 저장 메서드
    /***
     * vc 상태 저장은 src, trg, txt, trg_voice_id만 저장하면 됩니다
     * api로 생성된 오디오는 s3팀에서 만든 서비스를 호출하면 됩니다
     */
    @Operation(
            summary = "VC 상태 저장",
            description = "VC 프로젝트 상태를 저장합니다." )
    @PostMapping("/{projectId}/save")
    public ResponseDto vcSave(@RequestBody VCSaveDto vcSaveDto){
        // 프로젝트 id가 null인 경우에 새프로젝트 생성하는 서비스 호출
        // 반환값으로 생성된 project_id 값을 줌
        Long projectId;

        if(vcSaveDto.getProjectId() == null){
            projectId = vcService.createNewVCProject(vcSaveDto);
        } else {
            projectId = vcService.updateVCProject(vcSaveDto);
        }

        // 프로젝트 id가 존재하면 기존 프로젝트 업데이트하는 서비스 호출
        // 반환값으로 업데이트 된 project_id 값을 줌으로써 몇번 프로젝트가 업데이트 됐는지 확인용

        return DataResponseDto.of(projectId,"상태가 성공적으로 저장되었습니다.");
    }
    // try-catch문으로 감싸기

}
