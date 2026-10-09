package com.wakana.vefa.controller;

import com.wakana.vefa.dto.InformationSheetRequest;
import com.wakana.vefa.model.InformationSheet;
import com.wakana.vefa.services.InformationSheetService;
import com.wakana.vefa.util.FileTraferUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/information-sheets")
@RequiredArgsConstructor
public class InformationSheetController {

    private final InformationSheetService informationSheetService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadInformationSheet(@ModelAttribute InformationSheetRequest request) {
        try {
            
            String fileName = FileTraferUtil.handleFileUpload(request.getFile());
            request.setFileName(fileName );
            InformationSheet uploadedSheet = informationSheetService.uploadInformationSheet(request);
            return new ResponseEntity<>(uploadedSheet, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload information sheet: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/notary/{notaryId}")
    public ResponseEntity<List<InformationSheet>> getInformationSheetsByNotary(@PathVariable Long notaryId) {
        List<InformationSheet> sheets = informationSheetService.getInformationSheetsByNotary(notaryId);
        return ResponseEntity.ok(sheets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InformationSheet> getInformationSheet(@PathVariable Long id) {
        InformationSheet sheet = informationSheetService.getInformationSheet(id);
        return ResponseEntity.ok(sheet);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteInformationSheet(@PathVariable Long id) {
        try {
            informationSheetService.deleteInformationSheet(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }
}
