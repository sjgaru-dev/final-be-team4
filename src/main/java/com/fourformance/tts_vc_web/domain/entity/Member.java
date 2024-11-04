package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Entity
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue
    private Long id;

    private String email;
    private String pwd;
    private String name;
    private Integer gender;
    private LocalDateTime birthDate;
    private String phoneNumber;
    private String tou;
    private Boolean is_deleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // pwd 업데이트 메서드
    public void updatePassword(String newPwd) {
        this.pwd = newPwd;
        updateModifiedDate();
    }

    // phoneNumber 업데이트 메서드
    public void updatePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
        updateModifiedDate();
    }

    // is_deleted 업데이트 메서드
    public void markAsDeleted() {
        this.is_deleted = true;
        updateModifiedDate();
    }

    public void restore() {
        this.is_deleted = false;
        updateModifiedDate();
    }

    // 수정일 업데이트 메서드
    public void updateModifiedDate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Member 생성 메서드
    public static Member createMember(String email, String pwd, String name,
                                      Integer gender, LocalDateTime birthDate,
                                      String phoneNumber, String tou,
                                      Boolean isDeleted) {
        Member member = new Member();
        member.email = email;
        member.pwd = pwd;
        member.name = name;
        member.gender = gender;
        member.birthDate = birthDate;
        member.phoneNumber = phoneNumber;
        member.tou = tou;
        member.is_deleted = isDeleted != null ? isDeleted : false;
        member.createdAt = LocalDateTime.now();
        member.updatedAt = LocalDateTime.now();
        return member;
    }
}