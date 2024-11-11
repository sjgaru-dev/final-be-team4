package com.fourformance.tts_vc_web.dto.member;

import com.fourformance.tts_vc_web.domain.entity.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

@Getter
@Setter
@ToString
public class MemberTestDto {

    private Long id;
    private String pwd;
    private String name;
    private Integer gender;

    private static ModelMapper modelMapper = new ModelMapper();

    public Member createMember() {
        modelMapper.getConfiguration()
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper.map(this, Member.class);
    }

    public static MemberTestDto createMemberDto(Member member) {
        return modelMapper.map(member, MemberTestDto.class);
    }
}
