package com.fourformance.tts_vc_web.controller.concat;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.service.concat.ConcatService_team_api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/concat")
@Slf4j
public class ConcatController_team_api {

    private final ConcatService_team_api concatService;

    @PostMapping(value = "/convert/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto convertMultipleAudios(
            @RequestPart("sourceAudios") MultipartFile[] sourceAudios) {

        try {
            log.info("오디오 파일 병합 요청 수신: {}개 파일 업로드", sourceAudios.length);

            if (sourceAudios == null || sourceAudios.length == 0) {
                //"파일이 업로드되지 않았습니다."
                throw new BusinessException(ErrorCode.NOT_EXISTS_AUDIO);
            }

            // 서비스 호출
            String mergedFilePath = concatService.convertMultipleAudios(sourceAudios);

            log.info("병합된 파일 경로: {}", mergedFilePath);

            return DataResponseDto.of(mergedFilePath);
        } catch (Exception e) {
            log.error("오디오 병합 중 오류 발생", e);
            //"오디오 병합 중 오류가 발생했습니다."
            throw new BusinessException(ErrorCode.NOT_EXISTS_AUDIO);
        }
    }
}
