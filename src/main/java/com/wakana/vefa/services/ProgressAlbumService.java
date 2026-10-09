package com.wakana.vefa.services;

import com.wakana.vefa.dto.ProgressAlbumRequest;
import com.wakana.vefa.model.ProgressAlbum;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

public interface ProgressAlbumService {

    /**
     * -----------------------
     * CRUD ProgressAlbum
     * -----------------------
     */

    // Lorsqu'on ajoute un album, on invalide toutes les entrées du cache
    // "progressAlbums"
    @CacheEvict(value = "progressAlbums", allEntries = true)
    ProgressAlbum save(ProgressAlbumRequest request, List<String> pictureUrls);

    // Lorsqu'on met à jour un album, on invalide toutes les entrées du cache
    @CacheEvict(value = "progressAlbums", allEntries = true)
    ProgressAlbum update(Long id, ProgressAlbumRequest request, List<String> pictureUrls);

    // Lorsqu'on supprime un album, on invalide toutes les entrées du cache
    @CacheEvict(value = "progressAlbums", allEntries = true)
    void delete(Long id);

    /**
     * -----------------------
     * Recherche / Listes
     * -----------------------
     */

    // Mise en cache des albums par propriété immobilière
    //@Cacheable(value = "progressAlbums", key = "{'byProperty', #realEstatePropertyId}")
    List<ProgressAlbum> findByRealEstateProperty(Long realEstatePropertyId);

    void toggleArchive(Long albumId);
}
