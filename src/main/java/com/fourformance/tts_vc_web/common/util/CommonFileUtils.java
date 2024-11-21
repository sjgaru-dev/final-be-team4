package com.fourformance.tts_vc_web.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;

public class CommonFileUtils {

    private CommonFileUtils() {
        // 유틸리티 클래스는 인스턴스화를 방지합니다.
    }

    /**
     * File 객체를 MultipartFile로 변환하는 메서드
     *
     * @param file 변환할 File 객체
     * @param fileName 파일 이름
     * @return MultipartFile로 변환된 객체
     * @throws IOException 파일 처리 중 예외 발생
     */
    public static MultipartFile convertFileToMultipartFile(File file, String fileName) throws IOException {
        return new MultipartFile() {
            @Override
            public String getName() {
                return fileName;
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "audio/wav";
            }

            @Override
            public boolean isEmpty() {
                return file.length() == 0;
            }

            @Override
            public long getSize() {
                return file.length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Files.readAllBytes(file.toPath());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.copy(file.toPath(), dest.toPath());
            }
        };
    }
}
