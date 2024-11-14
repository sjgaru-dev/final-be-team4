package com.fourformance.tts_vc_web.controller.common;


import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.service.common.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service S3Service;

    // TTS나 VC로 반환한 유닛 오디오를 업로드하는 api
    @Operation(summary = "유닛(TTS or VC) 오디오 업로드", description = "유닛 오디오를 S3 버킷에 저장하고 메타데이터를 DB에 저장하는 api입니다."
            + "<br><br>매개변수 : <br>- 유닛 id, <br>- 프로젝트 id, <br>- 프로젝트 타입 (TTS, VC, Concat), <br>- 오디오 파일")
    @PostMapping(value = {"/tts/upload-generated-audio-to-bucket",
            "/vc/upload-generated-audio-to-bucket"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto uploadUnit(@RequestParam("file") MultipartFile file, @RequestParam("detailId") Long detailId,
                                  @RequestParam("projectId") Long projectId, HttpSession session) {
        Long userId = 0L; // 실제 프로젝트에서는 세션을 사용하여 사용자 ID를 가져옵니다.
//        Long userId = (Long) session.getAttribute("userId");

        try {
            // TTS & VC 반환 유닛 오디오 업로드 메서드 호출
            String fileUrl = S3Service.uploadUnitSaveFile(file, userId, projectId, detailId);
            return DataResponseDto.of(fileUrl, "파일이 성공적으로 업로드되었습니다.");

        } catch (Exception e) {
            // 구체적인 예외 지정은 추후에 할 예정
            throw new BusinessException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    // Concat으로 반환한 오디오를 업로드하는 api
    @Operation(summary = "Concat 오디오 업로드", description = "컨캣 오디오를 S3 버킷에 저장하고 메타데이터를 DB에 저장하는 api입니다."
            + "<br><br>매개변수 : <br>- 프로젝트 id, <br>- 오디오 파일")
    @PostMapping(value = "/concat/upload-generated-audio-to-bucket", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto uploadConcat(@RequestParam("file") MultipartFile file, @RequestParam("projectId") Long projectId,
                                    HttpSession session) throws IOException {
        Long userId = 0L; // 실제 프로젝트에서는 세션을 사용하여 사용자 ID를 가져옵니다.
//    Long userId = (Long) session.getAttribute("userId");
        try {
            // Concat 반환 유닛 오디오 업로드 메서드 호출
            String fileUrl = S3Service.uploadConcatSaveFile(file, userId, projectId); // 서비스 변경 후
            // 성공적인 응답 반환
            return DataResponseDto.of(fileUrl, "파일 업로드가 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            // 구체적인 예외 지정은 추후에 할 예정
            throw new BusinessException(ErrorCode.UNKNOWN_ERROR);
        }
    }

//    // 생성된 오디오를 다운받을 수 있는 presigned url을 제공하는 api
//    @Operation(summary = "생성된 오디오 다운로드", description = "TTS, VC, CONCAT으로 변환된 오디오를 S3 버킷으로부터 다운로드 받는 api입니다."
//            + "<br><br>매개변수:<br>- 프로젝트ID<br>- 유닛ID<br>- 오디오파일명" + "<br><br> 프로젝트 타입이 CONCAT일 경우는 유닛 ID를 null로 받습니다.")
//    @GetMapping(value = {"/tts/download-generated-audio-from-bucket", "/vc/download-generated-audio-from-bucket",
//            "/concat/download-generated-audio-from-bucket"})
//    public ResponseDto downloadGeneratedAudio(HttpSession session, @RequestParam("projectId") Long projectId,
//                                              @RequestParam(value = "detailId", required = false) Long detailId,
//                                              @RequestParam("fileName") String fileName) {
//        try {
//            Long userId = 0L;  // 개발 단계 임시 하드코딩
////            Long userId = (Long) session.getAttribute("userId");
//
//            // presigned url을 반환하는 서비스 호출
//            String presignedUrl = S3Service.generatePresignedUrl(userId, projectId, detailId, fileName);
//            return DataResponseDto.of(presignedUrl, "파일 다운로드 URL 생성 성공");
//        } catch (Exception e) {
//            // 예외는 추후에 정리할 예정s
//            throw new BusinessException(ErrorCode.UNKNOWN_ERROR);
//        }
//    }

    // 생성된 오디오를 다운받을 수 있는 presigned url을 제공하는 api
    @Operation(summary = "생성된 오디오 다운로드", description =
            "TTS, VC, CONCAT으로 변환된 오디오를 S3 버킷으로부터 다운로드 받을수 있는 URL을 제공하는 API 입니다." + "<br><br>매개변수:<br>- 버킷 경로")
    @GetMapping(value = {"/tts/download-generated-audio-from-bucket", "/vc/download-generated-audio-from-bucket",
            "/concat/download-generated-audio-from-bucket"})
    public ResponseDto downloadGeneratedAudio(@RequestParam("bucketRoute") String bucketRoute) {
        try {
            Long userId = 0L;  // 개발 단계 임시 하드코딩
//            Long userId = (Long) session.getAttribute("userId");

            // presigned url을 반환하는 서비스 호출
            String presignedUrl = S3Service.generatePresignedUrl(bucketRoute);
            return DataResponseDto.of(presignedUrl, "파일 다운로드 URL 생성 성공");
        } catch (Exception e) {
            // 예외는 추후에 정리할 예정s
            throw new BusinessException(ErrorCode.UNKNOWN_ERROR);
        }
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto uploadFiles(
            // 정상적인 처리에서는 List<String>을 반환하고, 예외가 발생할 경우 String으로 에러 메시지를 반환할 때 유용합니다.
            HttpSession session, @RequestParam("files") List<MultipartFile> files,
            @RequestParam("projectId") Long projectId, @RequestParam("audioType") String audioType) {
        try {

//            Long memberId = (Long)session.getAttribute("userId");
            Long memberId = 0L; // 개발 단계 임시 하드코딩

            AudioType enumAudioType = AudioType.valueOf(audioType);

            List<String> uploadedUrls = S3Service.uploadAndSaveMemberFile(files, memberId, projectId, enumAudioType);
            return DataResponseDto.of(uploadedUrls);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNKNOWN_ERROR);
        }
    }
}


