package com.wakana.vefa.services;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.wakana.vefa.dto.CommentaireRequest;
import com.wakana.vefa.model.Commentaire;

public interface CommenatireService {
    Page<Commentaire> getCommentairesByPropertyId(Long propertyId, Pageable pageable);

    Commentaire saveCommentaire(CommentaireRequest commentaireRequest);

    Commentaire ajouterReponse(CommentaireRequest commentaireRequest);
}