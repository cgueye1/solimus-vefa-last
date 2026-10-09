package com.wakana.vefa.dto;
import lombok.Data;

@Data
public class CommentaireRequest {
    private  String auteur;
    private String auteurimg; 
    private String date;
    private String contenu;  
    private Long idauteur;
    private Long idactu;
    private Long idcom;
   
   
   
}
