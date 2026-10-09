package com.wakana.vefa.controller;

import com.wakana.vefa.dto.DocsRequest;
import com.wakana.vefa.dto.SignatureRequest;
import com.wakana.vefa.dto.response.DocsResponse;
import com.wakana.vefa.services.DocsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class DocsController {

    private final DocsService docsService;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocsResponse> create(@ModelAttribute DocsRequest request) {
        return ResponseEntity.ok(docsService.create(request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocsResponse> update(
            @PathVariable Long id,
            @ModelAttribute DocsRequest request) {

        Optional<DocsResponse> response = docsService.update(id, request);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        docsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<DocsResponse>> findAll() {
        return ResponseEntity.ok(docsService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocsResponse> findById(@PathVariable Long id) {
        Optional<DocsResponse> response = docsService.findById(id);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<DocsResponse>> findByPropertyId(@PathVariable Long propertyId) {
        List<DocsResponse> docs = docsService.findByPropertyId(propertyId);
        docs.sort(Comparator.comparing(DocsResponse::getLastUpdated).reversed());
        return ResponseEntity.ok(docs);
    }

   /*  @PostMapping(value = "/{docId}/signed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocsResponse> uploadSignedPdf(
            @PathVariable Long docId,
            @RequestParam MultipartFile signedPdf) {
        return ResponseEntity.ok(docsService.addSignedPdf(docId, signedPdf));
    }*/

    // New endpoints for signature management

    @PostMapping(value = "/signature/mark", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocsResponse> markAsSigned(@ModelAttribute SignatureRequest signatureRequest) {
        return ResponseEntity.ok(docsService.markAsSigned(signatureRequest));
    }

    @PostMapping("/{documentId}/signatories")
    public ResponseEntity<DocsResponse> addSignatories(
            @PathVariable Long documentId,
            @RequestBody List<Long> userIds) {
        return ResponseEntity.ok(docsService.addSignatories(documentId, userIds));
    }

    @DeleteMapping("/{documentId}/signatories/{userId}")
    public ResponseEntity<DocsResponse> removeSignatory(
            @PathVariable Long documentId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(docsService.removeSignatory(documentId, userId));
    }

    @GetMapping("/pending/user/{userId}")
    public ResponseEntity<List<DocsResponse>> findPendingSignatures(@PathVariable Long userId) {
        return ResponseEntity.ok(docsService.findPendingSignaturesForUser(userId));
    }
}