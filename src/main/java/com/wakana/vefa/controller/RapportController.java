package com.wakana.vefa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wakana.vefa.dto.RapportRequest;
import com.wakana.vefa.model.Rapport;
import com.wakana.vefa.services.RapportService;
import com.wakana.vefa.util.FileTraferUtil;

import java.io.IOException;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {

    @Autowired
    private RapportService rapportService;

    @PostMapping("/save")
    public ResponseEntity<?> save(@ModelAttribute RapportRequest request) {
        try {
            String pdf = FileTraferUtil.handleFileUpload(request.getFile());
            Rapport rapport = rapportService.save(request, pdf);
            return ResponseEntity.ok(rapport);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save album: " + e.getMessage());
        }
    }



    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            rapportService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete album: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Page<Rapport> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @PathVariable Long id) {

        return rapportService.getAll(page, size, id);
    }

    @PutMapping("/archive/{id}")
    public ResponseEntity<Rapport> toggleArchive(@PathVariable Long id) {
        return ResponseEntity.ok(rapportService.toggleArchive(id));
    }
}
