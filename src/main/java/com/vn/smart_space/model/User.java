package com.vn.smart_space.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vn.smart_space.consts.EGender;
import com.vn.smart_space.consts.ERole;
import com.vn.smart_space.consts.EUserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User extends AbstractEntity {

    @Column(name = "full_name")
    String fullName;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Column(name = "phone")
    String phone;

    @Column(name = "email", unique = true)
    String email;

    @JsonIgnore
    @Column(name = "password")
    String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    EGender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    ERole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    EUserStatus status;

    @Column(name = "avatar_url")
    String avatarUrl;

}
