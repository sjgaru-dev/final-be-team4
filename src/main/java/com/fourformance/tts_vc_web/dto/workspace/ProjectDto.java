package com.fourformance.tts_vc_web.dto.workspace;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter@Setter@ToString@NoArgsConstructor@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String url;
    private String name;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

}