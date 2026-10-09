
package com.wakana.vefa.controller;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wakana.vefa.dto.CommentaireRequest;
import com.wakana.vefa.model.Commentaire;
import com.wakana.vefa.services.CommenatireService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/v1/commentaire")
@RequiredArgsConstructor
public class CommentaireController {
    private final CommenatireService commenatireService;

    @PostMapping("/save")
    public ResponseEntity<?> saveActu(@RequestBody CommentaireRequest commentaireRequest) {
        return ResponseEntity.ok(commenatireService.saveCommentaire(commentaireRequest));

    }

    @PostMapping("/repondre/save")
    public ResponseEntity<?> saveResponse(@RequestBody CommentaireRequest commentaireRequest) {
        return ResponseEntity.ok(commenatireService.ajouterReponse(commentaireRequest));

    }

    @GetMapping("/comments/{id:.+}")
    public ResponseEntity<?> getCommentairesByPropertyId(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Commentaire> commentairePage = commenatireService.getCommentairesByPropertyId(id, pageable);

        long totalElements = commentairePage.getTotalElements();
        int totalPages = commentairePage.getTotalPages();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements", String.valueOf(totalElements));
        responseHeaders.add("X-Total-Pages", String.valueOf(totalPages));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(commentairePage);

    }

}
