package com.wakana.vefa.controller;
import com.wakana.vefa.dto.ProgressAlbumRequest;
import com.wakana.vefa.model.ProgressAlbum;
import com.wakana.vefa.services.ProgressAlbumService;
import com.wakana.vefa.util.FileTraferUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/progress-album")
@RequiredArgsConstructor
public class ProgressAlbumController {

    private final ProgressAlbumService progressAlbumService;

    @PostMapping("/save")
    public ResponseEntity<?> save(@ModelAttribute ProgressAlbumRequest request) {
        try {
            List<String> pictureUrls = FileTraferUtil.uploadPictures(request.getPictures());
            ProgressAlbum savedAlbum = progressAlbumService.save(request, pictureUrls);
            return ResponseEntity.ok(savedAlbum);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save album: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @ModelAttribute ProgressAlbumRequest request) {
        try {
            List<String> pictureUrls =  FileTraferUtil.uploadPictures(request.getPictures());
            ProgressAlbum updatedAlbum = progressAlbumService.update(id, request, pictureUrls);
            return ResponseEntity.ok(updatedAlbum);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update album: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            progressAlbumService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete album: " + e.getMessage());
        }
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<ProgressAlbum>> getByRealEstateProperty(@PathVariable Long propertyId) {
        List<ProgressAlbum> albums = progressAlbumService.findByRealEstateProperty(propertyId);
        return ResponseEntity.ok(albums);
    }


 @PutMapping("/archive/{id}")
    public ResponseEntity<Void> toggleArchive(@PathVariable Long id) {
        progressAlbumService.toggleArchive(id);
        return ResponseEntity.noContent().build(); // 204
    }

}
