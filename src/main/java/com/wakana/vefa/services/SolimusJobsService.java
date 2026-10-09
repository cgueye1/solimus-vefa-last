package com.wakana.vefa.services;



import com.wakana.vefa.model.SolimusJobs;

import java.util.List;

public interface SolimusJobsService {
    SolimusJobs createJob(SolimusJobs job);
    SolimusJobs updateJob(Long id, SolimusJobs job);
    SolimusJobs getJobById(Long id);
    List<SolimusJobs> getAllJobs();
    void deleteJob(Long id);
}
