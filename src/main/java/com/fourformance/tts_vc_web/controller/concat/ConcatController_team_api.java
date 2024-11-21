package com.fourformance.tts_vc_web.controller.concat;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.controller.tts.TTSController_team_api;
import com.fourformance.tts_vc_web.dto.concat.ConcatRequestDetailDto;
import com.fourformance.tts_vc_web.dto.concat.ConcatRequestDto;
import com.fourformance.tts_vc_web.dto.concat.ConcatResponseDto;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.service.concat.ConcatService_team_api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/concat")
@Slf4j
public class ConcatController_team_api {

    private static final Logger LOGGER = Logger.getLogger(ConcatController_team_api.class.getName()); // 로깅 설정

    private final ConcatService_team_api concatService;

    @PostMapping(
            value = "/convert/batch",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    @Operation(
            summary = "오디오 파일 병합",
            description = "여러 오디오 파일을 업로드하고, 파일 사이에 무음을 추가하여 병합된 파일을 생성합니다.",
            tags = {"Audio Concat"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "병합 성공", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DataResponseDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseDto convertMultipleAudios(
            @RequestPart("concatRequestDto") @Parameter(description = "요청 DTO") ConcatRequestDto concatRequestDto,
            @RequestPart("files") @Parameter(description = "업로드할 파일들") List<MultipartFile> files
    )  {

        LOGGER.info("컨트롤러 메서드 호출됨: " + concatRequestDto); // 요청 데이터 로깅

        // 유효성 검증: 요청 데이터가 null이거나 텍스트 세부사항 리스트가 비어있는 경우 예외 처리
        if (concatRequestDto == null || concatRequestDto.getConcatRequestDetails() == null || concatRequestDto.getConcatRequestDetails().isEmpty()) {
            LOGGER.warning("유효하지 않은 요청 데이터"); // 잘못된 요청 데이터 로깅
            throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA); // 커스텀 예외 발생
        }

        try {

            // JSON과 파일 데이터 매핑
            List<ConcatRequestDetailDto> details = concatRequestDto.getConcatRequestDetails();
            if (details.size() != files.size()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
            }

            for (int i = 0; i < details.size(); i++) {
                details.get(i).setSourceAudio(files.get(i));
            }

            // 서비스 로직 호출
            // 서비스 계층에서 Concat 변환 로직 실행
            ConcatResponseDto concatResponse = concatService.convertAllConcatDetails(concatRequestDto);

            LOGGER.info("CONCAT 변환 성공");

            LOGGER.info("CONCAT 변환 성공"); // 변환 성공 로그
            // 성공적인 응답 데이터 반환
            return DataResponseDto.of(concatResponse);

        } catch (Exception e) {
            log.error("오디오 병합 중 오류 발생", e);
            //"오디오 병합 중 오류가 발생했습니다."
            throw new BusinessException(ErrorCode.NOT_EXISTS_AUDIO);
        }
    }
}
