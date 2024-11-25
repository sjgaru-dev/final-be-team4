package com.fourformance.tts_vc_web.common.util;


import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;

public class CustomMultipartFile_team_api implements MultipartFile {

    private final File file;
    private final String originalFileName;

    public CustomMultipartFile_team_api(File file) {
        this.file = file;
        this.originalFileName = file.getName();
    }

    @Override
    public String getName() {
        return originalFileName;
    }

    @Override
    public String getOriginalFilename() {
        return originalFileName;
    }

    @Override
    public String getContentType() {
        return null; // ContentType을 설정하려면 적절한 MIME 타입을 반환하도록 수정
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
        try (InputStream in = new FileInputStream(this.file);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
