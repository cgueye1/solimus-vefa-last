package com.wakana.vefa.controller;

import com.wakana.vefa.dto.JobsRequest;
import com.wakana.vefa.dto.MeetFileRequest;
import com.wakana.vefa.model.SolimusJobs;
import com.wakana.vefa.services.SolimusJobsService;
import com.wakana.vefa.util.FileTraferUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class SolimusJobsController {

    private final SolimusJobsService solimusJobsService;

  
    @PostMapping
    public ResponseEntity<?> createJob(@ModelAttribute JobsRequest request) {
      
        
        try{
        String icon =  FileTraferUtil.handleFileUpload(request.getFile());
        SolimusJobs job = new SolimusJobs();
        job.setName(request.getName());
        job.setIcon(icon);
        SolimusJobs createdJob = solimusJobsService.createJob(job);
        return ResponseEntity.ok(createdJob);
        
           
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update files: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolimusJobs> updateJob(@PathVariable Long id, @RequestBody SolimusJobs job) {
        SolimusJobs updatedJob = solimusJobsService.updateJob(id, job);
        return ResponseEntity.ok(updatedJob);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolimusJobs> getJobById(@PathVariable Long id) {
        SolimusJobs job = solimusJobsService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @GetMapping
    public ResponseEntity<List<SolimusJobs>> getAllJobs() {
        List<SolimusJobs> jobs = solimusJobsService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        solimusJobsService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
