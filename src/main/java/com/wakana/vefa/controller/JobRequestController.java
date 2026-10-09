package com.wakana.vefa.controller;

import com.wakana.vefa.dto.DevisRequest;
import com.wakana.vefa.dto.JobRequestRequest;
import com.wakana.vefa.model.JobRequest;
import com.wakana.vefa.services.JobRequestService;
import com.wakana.vefa.util.FileTraferUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/job-requests")
@RequiredArgsConstructor
public class JobRequestController {

    private final JobRequestService jobRequestService;

    @PostMapping
    public ResponseEntity<?> createJobRequest(@ModelAttribute JobRequestRequest request) {

        try {
            List<String> pictureUrls = FileTraferUtil.uploadPictures(request.getPictures());
            JobRequest jobRequest = jobRequestService.createJobRequest(request, pictureUrls);
            return ResponseEntity.ok(jobRequest);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save album: " + e.getMessage());
        }

    }

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<Page<JobRequest>> getRequestsByRequesterAndState(
            @PathVariable Long requesterId,
            @RequestParam String state,
            Pageable pageable) {
        Page<JobRequest> jobRequests = jobRequestService.getRequestsByRequesterAndState(requesterId, state, pageable);
        return ResponseEntity.ok(jobRequests);
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<Page<JobRequest>> getRequestsByProviderAndState(
            @PathVariable Long providerId,
            @RequestParam String state,
            Pageable pageable) {
        Page<JobRequest> jobRequests = jobRequestService.getRequestsByServiceProviderAndState(providerId, state,
                pageable);
        return ResponseEntity.ok(jobRequests);
    }

    @GetMapping("/service-provider/{serviceProviderId}/pending")
    public ResponseEntity<Page<JobRequest>> getPendingRequestsForServiceProvider(
            @PathVariable Long serviceProviderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JobRequest> jobRequests = jobRequestService.getPendingRequestsForServiceProvider(serviceProviderId,
                pageable);
        return ResponseEntity.ok(jobRequests);
    }

    @PostMapping("/{jobRequestId}/accept")
    public ResponseEntity<JobRequest> acceptJobRequest(
            @PathVariable Long jobRequestId,
            @RequestParam Long serviceProviderId) {
        JobRequest jobRequest = jobRequestService.acceptJobRequest(jobRequestId, serviceProviderId);
        return ResponseEntity.ok(jobRequest);
    }

    @GetMapping("/{jobRequestId}/refuse")
    public ResponseEntity<JobRequest> refuseJobRequest(
            @PathVariable Long jobRequestId) {
        JobRequest jobRequest = jobRequestService.refuseJobRequest(jobRequestId);
        return ResponseEntity.ok(jobRequest);
    }

    @GetMapping("/{jobRequestId}/done")
    public ResponseEntity<JobRequest> doneJobRequest(
            @PathVariable Long jobRequestId) {
        JobRequest jobRequest = jobRequestService.doneJobRequest(jobRequestId);
        return ResponseEntity.ok(jobRequest);
    }

    @GetMapping("/service-provider/{serviceProviderId}/accepted")
    public ResponseEntity<Page<JobRequest>> getAcceptedRequestsByServiceProvider(@PathVariable Long serviceProviderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JobRequest> jobRequests = jobRequestService.getAcceptedRequestsByServiceProvider(serviceProviderId,
                pageable);
        return ResponseEntity.ok(jobRequests);

    }

    @PostMapping("/devis")
    public ResponseEntity<?> addDevis(@ModelAttribute DevisRequest request) {
        try {
            String file = FileTraferUtil.handleFileUpload(request.getFile());
            return ResponseEntity.ok(jobRequestService.addDevis(request.getId(), file));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update files: " + e.getMessage());
        }
    }

}
