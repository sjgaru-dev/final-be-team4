package com.fourformance.tts_vc_web.controller.common;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.service.common.S3Service;
import com.fourformance.tts_vc_web.service.common.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;

import static com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta.createOutputAudioMeta;

@RestController
@RequiredArgsConstructor
public class S3Controller {


    private final AmazonS3Client amazonS3Client;

    private final S3Service S3Service;


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    // 절대경로
   private final String BASE_ROUTE2 = "https://"+ bucket + ".s3.amazonaws.com/";

    // TTS나 VC로 반환한 유닛 오디오를 업로드하는 api
    @Operation(
            summary = "유닛(TTS or VC) 오디오 업로드",
            description = "유닛 오디오를 S3 버킷에 저장하고 메타데이터를 DB에 저장하는 api입니다." +
                    "<br><br>매개변수 : <br>- 유닛 id, <br>- 프로젝트 id, <br>- 프로젝트 타입 (TTS, VC, Concat), <br>- 오디오 파일")
//    @PostMapping(value = "/upload_unit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping(value = {"/tts/upload-generated-audio-to-bucket", "/vc/upload-generated-audio-to-bucket"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadUnit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("detailId") Long detailId,
            @RequestParam("projectId") Long projectId,
            HttpSession session
    ) throws IOException {
        Long userId = 0L; // 실제 프로젝트에서는 세션을 사용하여 사용자 ID를 가져옵니다.
//        Long userId = (Long) session.getAttribute("userId");
        try {
            // TTS&VC 반환 유닛 오디오 업로드 메서드 호출
            String fileUrl = S3Service.uploadUnitSaveFile(file, userId, projectId, detailId); // 서비스 변경 후
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Concat으로 반환한 오디오를 업로드하는 api
    @Operation(
            summary = "Concat 오디오 업로드",
            description = "컨캣 오디오를 S3 버킷에 저장하고 메타데이터를 DB에 저장하는 api입니다." +
                    "<br><br>매개변수 : <br>- 프로젝트 id, <br>- 오디오 파일")
    @PostMapping(value = "/concat/upload-generated-audio-to-bucket", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadConcat(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            HttpSession session
    ) throws IOException {
        Long userId = 0L; // 실제 프로젝트에서는 세션을 사용하여 사용자 ID를 가져옵니다.
//        Long userId = (Long) session.getAttribute("userId");
        try {
            String fileUrl = S3Service.uploadConcatSaveFile(file, userId, projectId);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 생성된 오디오를 다운받을 수 있는 presigned url을 제공하는 api
    @Operation(
            summary = "생성된 오디오 다운로드",
            description = "TTS, VC, CONCAT으로 변환된 오디오를 S3 버킷으로부터 다운로드 받는 api입니다." +
                    "<br><br>매개변수:<br>- 프로젝트ID<br>- 유닛ID<br>- 오디오파일명" +
                    "<br><br> 프로젝트 타입이 CONCAT일 경우는 유닛 ID를 null로 받습니다.")
    @GetMapping(value={"/tts/download-generated-audio-from-bucket","/vc/download-generated-audio-from-bucket","/concat/download-generated-audio-from-bucket"})
    public ResponseEntity<String> downloadGeneratedAudio(
            HttpSession session,
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "detailId", required = false) Long detailId,
            @RequestParam("fileName") String fileName) throws Exception {
        try {
//            String userId = session.getAttribute("userId").toString();
            Long userId = 0L;  // 개발 단계 임시 하드코딩
//            Long userId = (Long) session.getAttribute("userId");

            // presigned url을 반환하는 서비스 호출
            String presignedUrl = S3Service.generatePresignedUrl(userId, projectId, detailId, fileName);
            return ResponseEntity.ok(presignedUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @PostMapping(value="/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFiles( // 정상적인 처리에서는 List<String>을 반환하고, 예외가 발생할 경우 String으로 에러 메시지를 반환할 때 유용합니다.
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("memberId") Long memberId,
            @RequestParam("projectId") Long projectId,
            @RequestParam("audioType") String audioType
    ) {
        try {
            AudioType enumAudioType = AudioType.valueOf(audioType);

            List<String> uploadedUrls = S3Service.uploadAndSaveMemberFile(files, memberId, projectId, enumAudioType);

            return ResponseEntity.ok(uploadedUrls);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("유효하지 않은 AudioType입니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 중 오류가 발생했습니다."+e.getMessage());
        }
    }
}


