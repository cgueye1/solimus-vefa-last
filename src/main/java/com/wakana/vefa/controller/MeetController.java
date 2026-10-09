package com.wakana.vefa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.dto.MeetFileRequest;
import com.wakana.vefa.dto.MeetRequest;
import com.wakana.vefa.dto.ProgressAlbumRequest;
import com.wakana.vefa.dto.response.MeetStatsResponse;
import com.wakana.vefa.model.Meet;
import com.wakana.vefa.model.MeetFiles;
import com.wakana.vefa.services.MeetFilesService;
import com.wakana.vefa.services.MeetService;
import com.wakana.vefa.util.FileTraferUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/meets")
@RequiredArgsConstructor
public class MeetController {

    @Autowired
    private MeetService meetService;
    private final MeetFilesService meetFilesService;

    @PostMapping
    public ResponseEntity<Meet> createMeet(@RequestBody MeetRequest meetRequest) {
        Meet newMeet = meetService.createMeet(meetRequest);
        return ResponseEntity.ok(newMeet);
    }

    @PostMapping("/cop")
    public ResponseEntity<Meet> createMeetCop(@RequestBody MeetRequest meetRequest) {
        Meet newMeet = meetService.createMeetCOP(meetRequest);
        return ResponseEntity.ok(newMeet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meet> updateMeet(@PathVariable Long id, @RequestBody MeetRequest meetRequest) {
        Meet updatedMeet = meetService.updateMeet(id, meetRequest);
        return ResponseEntity.ok(updatedMeet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeet(@PathVariable Long id) {
        meetService.deleteMeet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Meet>> getAllMeets() {
        List<Meet> meets = meetService.getAllMeets();
        return ResponseEntity.ok(meets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meet> getMeetById(@PathVariable Long id) {
        Meet meet = meetService.getMeetById(id);
        return ResponseEntity.ok(meet);
    }

    @PostMapping("/{meetId}/participants")
    public ResponseEntity<Void> addParticipants(@PathVariable Long meetId, @RequestBody List<Long> participantIds) {
        meetService.addParticipants(meetId, participantIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/creator/{creatorId}")
    public Page<Meet> getMeetsByCreator(
            @PathVariable Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam int month,
            @RequestParam int year) {

        Pageable pageable = PageRequest.of(page, size);
        return meetService.getMeetsByCreator(creatorId, month, year, pageable);
    }

    @GetMapping("/participant/{userId}")
    public Page<Meet> getMeetsByParticipant(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam int month,
            @RequestParam int year) {

        Pageable pageable = PageRequest.of(page, size);
        return meetService.getMeetsByParticipant(userId, month, year, pageable);
    }

    @GetMapping("/upcoming")
    public Page<Meet> getUpcomingMeets(@RequestParam Long realEstatePropertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return meetService.getUpcomingMeets(realEstatePropertyId, pageable);
    }

    @GetMapping("/past")
    public Page<Meet> getPastMeets(@RequestParam Long realEstatePropertyId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return meetService.getPastMeets(realEstatePropertyId, pageable);
    }

    @PostMapping(value = "/add/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<?> addFiles(@ModelAttribute MeetFileRequest request) {
        try {
            String file = FileTraferUtil.handleFileUpload(request.getFile());
            request.setFileName(file);

            return ResponseEntity.ok(meetFilesService.save(request));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update files: " + e.getMessage());
        }
    }

    @GetMapping("/meetfiles/{meetId}")
    public ResponseEntity<List<MeetFiles>> getFilesByMeetId(@PathVariable Long meetId) {
        List<MeetFiles> files = meetFilesService.getFilesByMeetId(meetId);
        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(files);
    }

    @GetMapping("/stats")
    public MeetStatsResponse getMeetStats(
            @RequestParam Long propertyId) {
        return meetService.getMeetStats(propertyId);
    }
}
