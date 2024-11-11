package com.fourformance.tts_vc_web.controller.common;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
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

import io.swagger.v3.oas.annotations.Parameter;

import static com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta.createOutputAudioMeta;

@RestController
@RequestMapping("/s3")
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
    @PostMapping(value = "/upload_unit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadUnit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("detailId") Long detailId,
            @RequestParam("projectId") Long projectId,
            HttpSession session
    ) throws IOException {
        String userId = "test"; // 실제 프로젝트에서는 세션을 사용하여 사용자 ID를 가져옵니다.
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

    // VC로 반환한 오디오를 업로드하는 api
    @Operation(
            summary = "Concat 오디오 업로드",
            description = "컨캣 오디오를 S3 버킷에 저장하고 메타데이터를 DB에 저장하는 api입니다." +
                    "<br><br>매개변수 : <br>- 프로젝트 id, <br>- 오디오 파일")
    @PostMapping(value = "/upload_concat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadConcat(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            HttpSession session
    ) throws IOException {
        String userId = "test"; // 실제 프로젝트에서는 세션을 사용하여 사용자 ID를 가져옵니다.
//        Long userId = (Long) session.getAttribute("userId");
        try {
            String fileUrl = S3Service.uploadConcatSaveFile(file, userId, projectId);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    // 임시로 해보는거
//    @GetMapping("/{userId}/{projectId}/{ttsDetailId}/{fileName}")
//    public ResponseEntity<String> downloadTTS(
//            @PathVariable Long userId, @PathVariable Long projectId, @PathVariable Long ttsDetailId, @PathVariable String fileName) throws IOException {
//        return generatePresignedUrl(userId, projectId, ttsDetailId, fileName); // url만들기
//    }
//
//    // url만드는 메서드
//    private ResponseEntity<String> generatePresignedUrl(Long userId, Long projectId, Long ttsDetailId, String fileName) {
//        try {
//            // 파일경롱를 생성
//            String Filepath = userId + "/" + projectId + "/" + ttsDetailId + "/" + fileName;
//
////         presignedurl생성요청 + 제한시간 걸어줘야함 ( 보안 문제 )
//            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, Filepath);
//            request.withMethod(com.amazonaws.HttpMethod.GET)
//                    .withExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
//
//            URL presignedUrl = amazonS3Client.generatePresignedUrl(request);
//
//            return ResponseEntity.ok(presignedUrl.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//    }

    @GetMapping("presigned_url")
    public ResponseEntity<String> downloadTTS(
            @RequestParam Long userId, @RequestParam Long projectId, @RequestParam Long ttsDetailId, @RequestParam String fileName) throws Exception {
        try {
            String presignedUrl = S3Service.generatePresignedUrl(userId, projectId, ttsDetailId, fileName);
            return ResponseEntity.ok(presignedUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{userId}/{projectId}/{vcDetailId}/{fileName}")
    public ResponseEntity<String> downloadVC(
            @PathVariable Long userId, @PathVariable Long projectId, @PathVariable Long vcDetailId, @PathVariable String fileName
    ) throws IOException {
        return vcGeneratePresignedUrl(userId, projectId, vcDetailId, fileName);
    }

    private ResponseEntity<String> vcGeneratePresignedUrl(Long userId, Long projectId, Long vcDetailId, String fileName) {
        String path = userId + "/" + projectId + "/" + vcDetailId + "/" + fileName;
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, path);
        request.withMethod(com.amazonaws.HttpMethod.GET)
                .withExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
        URL presignedUrl = amazonS3Client.generatePresignedUrl(request);
        return ResponseEntity.ok(presignedUrl.toString()); // url을 반환하며, ok처리.
    }
//    @GetMapping("/download_vc")
//    public ResponseEntity<String> downloadVC(@RequestParam("file") MultipartFile file, HttpSession session, Long projectId, Long vcDetailId) throws IOException {
//
//    }
//
//    @GetMapping("/download_concat")
//    public ResponseEntity<String> downloadConcat(@RequestParam("file" MultipartFile file, HttpSession session, Long projectId) throws IOException { }


}
