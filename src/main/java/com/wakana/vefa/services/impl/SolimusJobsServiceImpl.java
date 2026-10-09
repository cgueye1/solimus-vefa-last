package com.wakana.vefa.services.impl;
import com.wakana.vefa.model.SolimusJobs;
import com.wakana.vefa.repository.SolimusJobsRepository;
import com.wakana.vefa.services.SolimusJobsService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SolimusJobsServiceImpl implements SolimusJobsService {

    private final SolimusJobsRepository solimusJobsRepository;

    public SolimusJobsServiceImpl(SolimusJobsRepository solimusJobsRepository) {
        this.solimusJobsRepository = solimusJobsRepository;
    }

    @Override
    public SolimusJobs createJob(SolimusJobs job) {
        return solimusJobsRepository.save(job);
    }

    @Override
    public SolimusJobs updateJob(Long id, SolimusJobs job) {
        SolimusJobs existingJob = solimusJobsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
        existingJob.setName(job.getName());
        return solimusJobsRepository.save(existingJob);
    }

    @Override
    public SolimusJobs getJobById(Long id) {
        return solimusJobsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
    }

    @Override
    public List<SolimusJobs> getAllJobs() {
        return solimusJobsRepository.findAll();
    }

    @Override
    public void deleteJob(Long id) {
        if (!solimusJobsRepository.existsById(id)) {
            throw new IllegalArgumentException("Job not found with id: " + id);
        }
        solimusJobsRepository.deleteById(id);
    }
}
