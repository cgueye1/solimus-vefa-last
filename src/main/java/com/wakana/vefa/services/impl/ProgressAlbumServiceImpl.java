package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.ProgressAlbumRequest;
import com.wakana.vefa.model.ProgressAlbum;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.repository.ProgressAlbumRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.services.ProgressAlbumService;
import com.wakana.vefa.util.XssSanitizer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressAlbumServiceImpl implements ProgressAlbumService {

    private final ProgressAlbumRepository progressAlbumRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    @Override
    public ProgressAlbum save(ProgressAlbumRequest request, List<String> pictureUrls) {
        RealEstateProperty property = realEstatePropertyRepository.findById(request.getRealEstatePropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid real estate property ID"));

        ProgressAlbum album = new ProgressAlbum(XssSanitizer.clean(request.getPhaseName()), property,
                request.isEntrance(), pictureUrls);
        album.setDescription(XssSanitizer.clean(request.getDescription()));
        return progressAlbumRepository.save(album);
    }

    @Override
    public ProgressAlbum update(Long id, ProgressAlbumRequest request, List<String> pictureUrls) {
        ProgressAlbum album = progressAlbumRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid album ID"));

        RealEstateProperty property = realEstatePropertyRepository.findById(request.getRealEstatePropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid real estate property ID"));

        album.setPhaseName(XssSanitizer.clean(request.getPhaseName()));
        album.setRealEstateProperty(property);
        // Only update pictures if the new list is not empty
        if (pictureUrls != null && !pictureUrls.isEmpty()) {
            List<String> existingPictures = album.getPictures();
            if (existingPictures == null) {
                existingPictures = new ArrayList<>();
            }
            existingPictures.addAll(pictureUrls); // Add the new pictures to the existing ones
            album.setPictures(existingPictures);
        }
        album.setDescription(XssSanitizer.clean(request.getDescription()));
        return progressAlbumRepository.save(album);
    }

    @Override
    public void delete(Long id) {
        progressAlbumRepository.deleteById(id);
    }

    @Override
    public List<ProgressAlbum> findByRealEstateProperty(Long realEstatePropertyId) {
        return progressAlbumRepository.findByRealEstatePropertyId(realEstatePropertyId);
    }

    @Override
    public void toggleArchive(Long albumId) {
        ProgressAlbum album = progressAlbumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("ProgressAlbum introuvable"));

        // Toggle archive
        album.setArchived(!album.isArchived());
        album.setLastUpdated(LocalDateTime.now());

        progressAlbumRepository.save(album);
    }
}
