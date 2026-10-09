package com.wakana.vefa.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wakana.vefa.dto.CommentaireRequest;
import com.wakana.vefa.model.Commentaire;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.repository.CommentaireRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.services.CommenatireService;

@Service
@RequiredArgsConstructor
public class CommentaireServiceImpl implements CommenatireService {

    private final CommentaireRepository commentaireRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    public Commentaire saveCommentaire(CommentaireRequest commentaireRequest) {
        RealEstateProperty actu = realEstatePropertyRepository.findById(commentaireRequest.getIdactu()).orElse(null);
        actu.setCommentcount(actu.getCommentcount() + 1);
        RealEstateProperty savedActu = realEstatePropertyRepository.save(actu);

        Commentaire commentaire = new Commentaire();
        commentaire.setAuteur(commentaireRequest.getAuteur());
        commentaire.setAuteurimg(commentaireRequest.getAuteurimg());
        commentaire.setContenu(commentaireRequest.getContenu());
        commentaire.setDate(commentaireRequest.getDate());
        commentaire.setProperty(savedActu);
        commentaire.setIdauteur(commentaireRequest.getIdauteur());

        return commentaireRepository.save(commentaire);
    }

    public Commentaire ajouterReponse(CommentaireRequest commentaireRequest) {
        Commentaire commentaire = commentaireRepository.findById(commentaireRequest.getIdcom()).orElse(null);

        if (null == commentaire) {

            return null;
        }
        commentaire.getProperty().setCommentcount(commentaire.getProperty().getCommentcount() + 1);
        RealEstateProperty savedActu = realEstatePropertyRepository.save(commentaire.getProperty());

        Commentaire response = new Commentaire();
        response.setAuteur(commentaireRequest.getAuteur());
        response.setAuteurimg(commentaireRequest.getAuteurimg());
        response.setContenu(commentaireRequest.getContenu());
        response.setDate(commentaireRequest.getDate());
        response.setParent(commentaire);
        response.setProperty(savedActu);
        response.setIdauteur(commentaireRequest.getIdauteur());

        Commentaire savedResponse = commentaireRepository.save(response);
        commentaire.getReponses().add(savedResponse);
        return commentaireRepository.save(commentaire);

    }

    public Page<Commentaire> getCommentairesByPropertyId(Long propertyId, Pageable pageable) {
        return commentaireRepository.findByPropertyIdAndParentIsNullOrderByDateDesc(propertyId, pageable);
    }

}
