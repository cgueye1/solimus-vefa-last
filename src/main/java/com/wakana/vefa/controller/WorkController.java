package com.wakana.vefa.controller;

import com.wakana.vefa.dto.MeetFileRequest;
import com.wakana.vefa.dto.WorkRequest;
import com.wakana.vefa.dto.response.RealEstatePropertyKpiResponse;
import com.wakana.vefa.dto.response.WorkKpiResponse;
import com.wakana.vefa.model.MeetFiles;
import com.wakana.vefa.model.Work;
import com.wakana.vefa.model.WorkFiles;
import com.wakana.vefa.services.WorkService;
import com.wakana.vefa.util.FileTraferUtil;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @PostMapping("/{propertyId}")
    public Work createWork(@PathVariable Long propertyId, @RequestBody WorkRequest request) {
        return workService.createWork(propertyId, request);
    }

    @PutMapping("/{id}")
    public Work updateWork(@PathVariable Long id, @RequestBody WorkRequest request) {
        return workService.updateWork(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteWork(@PathVariable Long id) {
        workService.deleteWork(id);
    }

    @GetMapping("/property/{propertyId}")
    public Page<Work> listWorksByProperty(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String month // format yyyy-MM
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return workService.listWorksByProperty(propertyId, month, pageable);
    }

    @PutMapping("/{id}/status")
    public Work changeStatus(@PathVariable Long id, @RequestParam String status) {
        return workService.changeStatus(id, status);
    }

    @PostMapping("/{id}/pictures")
    public ResponseEntity<Work> uploadPictures(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(workService.addPictures(id, files));
    }

    @DeleteMapping("/{id}/pictures")
    public ResponseEntity<Work> deletePicture(
            @PathVariable Long id,
            @RequestParam("url") String pictureUrl) {
        return ResponseEntity.ok(workService.removePicture(id, pictureUrl));
    }

    @GetMapping("/{workId}/kpi")
    public WorkKpiResponse getWorkKpi(@PathVariable Long workId) {
        return workService.getKpiByWork(workId);
    }
    
    @GetMapping("/{workId}")
    public Work getWorkDetails(@PathVariable Long workId) {
        return workService.getworkDetail(workId).isPresent()?workService.getworkDetail(workId).get():null;
    }


    @GetMapping("/property/{propertyId}/kpi")
    public RealEstatePropertyKpiResponse getPropertyKpi(
            @PathVariable Long propertyId,
            @RequestParam(required = false) String month // format yyyy-MM
    ) {
        return workService.getKpi(propertyId, month);
    }

    @PostMapping("/add/file")
    public ResponseEntity<?> addFiles(@ModelAttribute MeetFileRequest request) {
        try {
            String file = FileTraferUtil.handleFileUpload(request.getFile());
            request.setFileName(file);

            return ResponseEntity.ok(workService.addFile(request));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update files: " + e.getMessage());
        }
    }

    @GetMapping("/workfiles/{workId}")
    public ResponseEntity<?> getFilesByWorkId(@PathVariable Long workId) {
        List<WorkFiles> files = workService.getFilesByWorkId(workId);
        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(files);
    }
}
