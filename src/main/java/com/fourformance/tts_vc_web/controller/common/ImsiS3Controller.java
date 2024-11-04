package com.fourformance.tts_vc_web.controller.common;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class ImsiS3Controller {


    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    // 절대경로
    private final String BASE_ROUTE = "https://popomance.s3.amazonaws.com/";


    @PostMapping("/upload_unit")
    public ResponseEntity<String> uploadUnit(@RequestParam("file") MultipartFile file, HttpSession session, Long detailId,Long projectId) throws IOException {


        String userId = "test";
//        Long userId = (Long) session.getAttribute("userId");
        try{
            // 날짜 포맷 지정
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = formatter.format(new Date());  // 예: "20231027_235959"

            // 파일 이름 형식: "날짜시간_원본파일명"
            String fileName = timeStamp +".wav";  // 예: "20231027_235959.wav"

//            String fileName = file.getOriginalFilename();
            String fileUrl = BASE_ROUTE + "/"+ userId +"/"+projectId+"/"+detailId+"/"+fileName;
            ObjectMetadata metadata = new ObjectMetadata(); // 공백 수정
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);
            return ResponseEntity.ok(fileUrl);


        }catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/upload_concat")
    public ResponseEntity<String> uploadConcat(@RequestParam("file") MultipartFile file, HttpSession session,Long projectId) throws IOException {

        try {
            // 날짜 포맷 지정
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = formatter.format(new Date());  // 예: "20231027_235959"

            // 파일 이름 형식: "날짜시간_원본파일명"
            String fileName = timeStamp +".wav";  // 예: "20231027_235959.wav"

//            String fileName = file.getOriginalFilename();
            String userId = "test";
//            String fileName = file.getOriginalFilename();
            String fileUrl = BASE_ROUTE + "/" + userId + "/" + projectId + "/" + fileName;
            ObjectMetadata metadata = new ObjectMetadata(); // 공백 수정
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);
            return ResponseEntity.ok(fileUrl);
        }catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
