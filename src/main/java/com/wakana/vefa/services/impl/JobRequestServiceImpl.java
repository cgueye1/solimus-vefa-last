package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.JobRequestRequest;
import com.wakana.vefa.enums.RequestStateEnum;
import com.wakana.vefa.model.JobRequest;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.SolimusJobs;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.JobRequestRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.SolimusJobsRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.JobRequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class JobRequestServiceImpl implements JobRequestService {

    private final JobRequestRepository jobRequestRepository;
    private final SolimusJobsRepository solimusJobsRepository;
    private final UserRepository userRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    @Override
    public JobRequest createJobRequest(JobRequestRequest request, List<String> pictureUrls) {
        SolimusJobs job = solimusJobsRepository.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        /*
         * User serviceProvider =
         * userRepository.findById(request.getServiceProviderId())
         * .orElseThrow(() -> new
         * IllegalArgumentException("Service Provider not found"));
         */

        RealEstateProperty property = realEstatePropertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        JobRequest jobRequest = new JobRequest();
        jobRequest.setJob(job);
        jobRequest.setDescription(request.getDescription());
        jobRequest.setRequester(requester);
        jobRequest.setProperty(property);
        jobRequest.setState(RequestStateEnum.PENDING);
        jobRequest.setPictures(pictureUrls);

        // Save the JobRequest
        return jobRequestRepository.save(jobRequest);
    }

    @Override
    public Page<JobRequest> getRequestsByRequesterAndState(Long requesterId, String state, Pageable pageable) {
        RequestStateEnum stateEnum = RequestStateEnum.valueOf(state.toUpperCase());
        return jobRequestRepository.findAllByRequesterIdAndState(requesterId, stateEnum, pageable);
    }

    @Override
    public Page<JobRequest> getPendingRequestsForServiceProvider(Long serviceProviderId, Pageable pageable) {
        return jobRequestRepository.findPendingRequestsByServiceProviderDomain(serviceProviderId,
                RequestStateEnum.PENDING, pageable);
    }

    @Override
    public JobRequest acceptJobRequest(Long jobRequestId, Long serviceProviderId) {
        Optional<JobRequest> jobRequestOpt = jobRequestRepository.findById(jobRequestId);

        if (jobRequestOpt.isEmpty()) {
            throw new IllegalArgumentException("JobRequest not found");
        }

        JobRequest jobRequest = jobRequestOpt.get();

        if (!jobRequest.getState().equals(RequestStateEnum.PENDING)) {
            throw new IllegalStateException("Only pending requests can be accepted");
        }

        jobRequest.setServiceProvider(userRepository.findById(serviceProviderId)
                .orElseThrow(() -> new IllegalArgumentException("ServiceProvider not found")));
        jobRequest.setState(RequestStateEnum.ACCEPTED);
        return jobRequestRepository.save(jobRequest);
    }

    @Override
    public JobRequest refuseJobRequest(Long jobRequestId) {
        Optional<JobRequest> jobRequestOpt = jobRequestRepository.findById(jobRequestId);

        if (jobRequestOpt.isEmpty()) {
            throw new IllegalArgumentException("JobRequest not found");
        }

        JobRequest jobRequest = jobRequestOpt.get();

        if (!jobRequest.getState().equals(RequestStateEnum.ACCEPTED)) {
            throw new IllegalStateException("Only ACCEPTED requests can be accepted");
        }

        jobRequest.setServiceProvider(null);
        jobRequest.setState(RequestStateEnum.PENDING);
        jobRequest.setDevis("");

        return jobRequestRepository.save(jobRequest);
    }

    public JobRequest doneJobRequest(Long jobRequestId) {
        Optional<JobRequest> jobRequestOpt = jobRequestRepository.findById(jobRequestId);

        if (jobRequestOpt.isEmpty()) {
            throw new IllegalArgumentException("JobRequest not found");
        }

        JobRequest jobRequest = jobRequestOpt.get();

        if (!jobRequest.getState().equals(RequestStateEnum.ACCEPTED)) {
            throw new IllegalStateException("Only ACCEPTED requests can be accepted");
        }

        jobRequest.setState(RequestStateEnum.COMPLETED);

        return jobRequestRepository.save(jobRequest);
    }

    @Override
    public Page<JobRequest> getAcceptedRequestsByServiceProvider(Long serviceProviderId, Pageable pageable) {
        return jobRequestRepository.findAcceptedRequestsByServiceProvider(serviceProviderId, RequestStateEnum.ACCEPTED,
                pageable);
    }

    @Override
    public JobRequest addDevis(Long jobRequestId, String devis) {
        Optional<JobRequest> jobRequestOpt = jobRequestRepository.findById(jobRequestId);

        if (jobRequestOpt.isEmpty()) {
            throw new IllegalArgumentException("JobRequest not found");
        }
        JobRequest jobRequest = jobRequestOpt.get();
        jobRequest.setDevis(devis);

        return jobRequestRepository.save(jobRequest);

    }

    @Override
    public Page<JobRequest> getRequestsByServiceProviderAndState(Long id, String state, Pageable pageable) {
        RequestStateEnum stateEnum = RequestStateEnum.valueOf(state.toUpperCase());
        return jobRequestRepository.findAllByServiceProviderIdAndState(id, stateEnum, pageable);
    }

}
