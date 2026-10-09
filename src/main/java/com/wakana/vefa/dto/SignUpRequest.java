package com.wakana.vefa.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SignUpRequest {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private boolean activated;
    private String telephone;
    private String role;
    private String profil;
    private String date;
    private String lieunaissance;
    private String adress;
    private boolean notifiable;
    private String subject;
    private String html;
    private String compagnyName;
    private double funds;

    private Long propretyId;
    private Long userId;
    private double area;

    private Long jobId;

    private MultipartFile file;

}
