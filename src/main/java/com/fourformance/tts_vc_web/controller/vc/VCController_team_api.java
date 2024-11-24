package com.fourformance.tts_vc_web.controller.vc;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
import com.fourformance.tts_vc_web.dto.vc.VCDetailResDto;
import com.fourformance.tts_vc_web.service.vc.VCService_team_api;
import com.fourformance.tts_vc_web.service.vc.VCService_team_api2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vc")
@Tag(name = "Voice Conversion API", description = "Voice Conversion 관련 기능을 제공합니다.")
public class VCController_team_api {

    private static final Logger LOGGER = Logger.getLogger(VCController_team_api.class.getName());
    private final VCService_team_api vcService;
    private final VCService_team_api2 new_vcService;

//    @Operation(summary = "VC 프로젝트 처리", description = "소스/타겟 오디오 파일 처리 및 Voice ID 생성")
//    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "VC 프로젝트 처리 성공")})
//    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseDto processVCProject(
//            @RequestParam("memberId") Long memberId,
//            @RequestPart("vcSaveDto") VCSaveDto vcSaveDto,
//            @RequestPart("files") List<MultipartFile> files) {
//
//        try {
//            LOGGER.info("VC 프로젝트 처리 요청 수신");
//            List<VCDetailResDto> response = vcService.processVCProject(vcSaveDto, files, memberId);
//            LOGGER.info("VC 프로젝트 처리 완료");
//            return DataResponseDto.of(response);
//        } catch (Exception e) {
//            LOGGER.severe("VC 프로젝트 처리 실패: " + e.getMessage());
//            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
//        }
//    }

    // VC 오디오 생성 수정 중
    @Operation(summary = "VC 프로젝트 처리", description = "소스/타겟 오디오 파일 처리 및 Voice ID 생성")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "VC 프로젝트 처리 성공")})
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto processVCProject(
            @RequestParam("memberId") Long memberId,
            @RequestPart("vcSaveDto") VCSaveDto vcSaveDto,
            @RequestPart("files") List<MultipartFile> files) {

            List<VCDetailResDto> response = new_vcService.processVCProject(vcSaveDto, files, memberId);
            return DataResponseDto.of(response);

    }
}
