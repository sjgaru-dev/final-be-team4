package com.fourformance.tts_vc_web.service.concat;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConcatService_temp {

    public String concatAudioFiles(MultipartFile[] files, int silenceDuration) throws IOException, UnsupportedAudioFileException {
        List<AudioInputStream> audioInputStreams = new ArrayList<>();
        AudioFormat commonFormat = null; // 공통 포맷 저장

        for (MultipartFile file : files) {
            // MultipartFile을 임시 파일로 저장하여 InputStream이 닫히는 문제 방지
            File tempFile = File.createTempFile("audio", ".wav");
            file.transferTo(tempFile);

            // AudioInputStream 생성
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(tempFile);

            // 첫 번째 파일 포맷을 기준으로 설정
            if (commonFormat == null) {
                commonFormat = audioInputStream.getFormat();
            } else {
                // 포맷이 일치하지 않으면 변환
                if (!audioInputStream.getFormat().matches(commonFormat)) {
                    audioInputStream = convertToCommonFormat(audioInputStream, commonFormat);
                }
            }

            audioInputStreams.add(audioInputStream);

            // 침묵 구간 추가
            AudioInputStream silenceStream = createSilenceStream(commonFormat, silenceDuration);
            audioInputStreams.add(silenceStream);

            // 임시 파일 삭제
            tempFile.delete();
        }

        // 마지막 오디오 파일 뒤의 침묵 구간은 제외
        if (!audioInputStreams.isEmpty()) {
            audioInputStreams.remove(audioInputStreams.size() - 1);
        }

        // 모든 AudioInputStream을 연결하여 최종 파일 생성
        AudioInputStream concatenatedStream = audioInputStreams.get(0);

        // 모든 오디오 스트림을 합치는 로직 개선
        for (int i = 1; i < audioInputStreams.size(); i++) {
            concatenatedStream = mergeAudioStreams(concatenatedStream, audioInputStreams.get(i));
        }

        // 결과 파일 저장 경로
        String outputPath = "output/concatenatedAudio.wav";
        File outputDir = new File("output");

        // 디렉토리 없으면 생성
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File(outputPath);

        // 결과 파일 저장
        AudioSystem.write(concatenatedStream, AudioFileFormat.Type.WAVE, outputFile);

        // 모든 스트림을 닫아 자원 해제
        for (AudioInputStream ais : audioInputStreams) {
            ais.close();
        }

        return outputPath;
    }

    // 오디오 파일을 공통 포맷으로 변환하는 메서드
    private AudioInputStream convertToCommonFormat(AudioInputStream audioInputStream, AudioFormat commonFormat) throws IOException {
        AudioFormat.Encoding encoding = commonFormat.getEncoding();
        AudioFormat newFormat = new AudioFormat(encoding, commonFormat.getSampleRate(), commonFormat.getSampleSizeInBits(),
                commonFormat.getChannels(), commonFormat.getFrameSize(), commonFormat.getFrameRate(), commonFormat.isBigEndian());
        return AudioSystem.getAudioInputStream(newFormat, audioInputStream);
    }

    // 침묵 구간을 생성하여 AudioInputStream으로 반환
    private AudioInputStream createSilenceStream(AudioFormat format, int durationInSeconds) {
        int silenceBytes = (int) (format.getFrameSize() * format.getFrameRate() * durationInSeconds);
        byte[] silenceData = new byte[silenceBytes];
        return new AudioInputStream(new ByteArrayInputStream(silenceData), format, silenceBytes / format.getFrameSize());
    }

    // 두 개의 AudioInputStream을 합치는 메서드
    private AudioInputStream mergeAudioStreams(AudioInputStream stream1, AudioInputStream stream2) throws IOException, UnsupportedAudioFileException {
        // 오디오 포맷이 동일한지 확인
        if (!stream1.getFormat().matches(stream2.getFormat())) {
            throw new UnsupportedAudioFileException("Audio formats do not match.");
        }

        // 스트림을 합치기
        return new AudioInputStream(new SequenceInputStream(stream1, stream2),
                stream1.getFormat(),
                stream1.getFrameLength() + stream2.getFrameLength());
    }
}
