package com.fourformance.tts_vc_web.controller.common;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fourformance.tts_vc_web.service.common.ImsiS3Service;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta.createOutputAudioMeta;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class ImsiS3Controller {


    private final AmazonS3Client amazonS3Client;

    private final ImsiS3Service imsiS3Service;


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    // 절대경로
    private final String BASE_ROUTE = "https://popomance.s3.amazonaws.com/";


    @PostMapping("/upload_unit")
    public ResponseEntity<String> uploadUnit(@RequestParam("file") MultipartFile file, HttpSession session, Long detailId,Long projectId,String projectType) throws IOException {
        String userId = "test"; // test용, 원래는 세션 씁니다.
//        Long userId = (Long) session.getAttribute("userId");

        try{

        String fileUrl = imsiS3Service.uploadUnitSaveFile(file, userId, projectId, detailId, projectType);
        return ResponseEntity.ok(fileUrl);
            // OutputAudiometa에 save하는 코드가 없음  업로드에서 끝나는게 아니라, 그 경로를 저장해야함.
        }catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/upload_concat")
    public ResponseEntity<String> uploadConcat(@RequestParam("file") MultipartFile file, HttpSession session,Long projectId) throws IOException {

        try {

            String userId ="test";
            String fileUrl = imsiS3Service.uploadConcatSaveFile(file, userId, projectId);

            return ResponseEntity.ok(fileUrl);
        }catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    // 임시로 해보는거
    @GetMapping("/{userId}/{projectId}/{ttsDetailId}/{fileName}")
    public ResponseEntity<String> downloadTTS(
            @PathVariable Long userId,@PathVariable Long projectId,@PathVariable Long ttsDetailId,@PathVariable String fileName) throws IOException {
        return generatePresignedUrl(userId, projectId, ttsDetailId,fileName); // url만들기
    }

    // url만드는 메서드
    private ResponseEntity<String> generatePresignedUrl(Long userId, Long projectId, Long ttsDetailId, String fileName) {
        try {
            // 파일경롱를 생성
            String Filepath = userId + "/" + projectId + "/" + ttsDetailId + "/" + fileName;

//         presignedurl생성요청 + 제한시간 걸어줘야함 ( 보안 문제 )
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, Filepath);
            request.withMethod(com.amazonaws.HttpMethod.GET)
                    .withExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5));

            URL presignedUrl = amazonS3Client.generatePresignedUrl(request);

            return ResponseEntity.ok(presignedUrl.toString());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{userId}/{projectId}/{vcDetailId}/{fileName}")
    public ResponseEntity<String> downloadVC(
            @PathVariable Long userId, @PathVariable Long projectId,@PathVariable Long vcDetailId,@PathVariable String fileName
                                                )throws IOException {
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
