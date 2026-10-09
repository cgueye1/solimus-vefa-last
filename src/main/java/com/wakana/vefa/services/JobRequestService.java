package com.wakana.vefa.services;

import com.wakana.vefa.dto.JobRequestRequest;
import com.wakana.vefa.model.JobRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface JobRequestService {
    JobRequest createJobRequest(JobRequestRequest request, List<String> pictureUrls);

    Page<JobRequest> getRequestsByRequesterAndState(Long requesterId, String state, Pageable pageable);

    Page<JobRequest> getRequestsByServiceProviderAndState(Long requesterId, String state, Pageable pageable);

    Page<JobRequest> getPendingRequestsForServiceProvider(Long serviceProviderId, Pageable pageable);

    JobRequest acceptJobRequest(Long jobRequestId, Long serviceProviderId);

    JobRequest refuseJobRequest(Long jobRequestId);

    JobRequest doneJobRequest(Long jobRequestId);

    JobRequest addDevis(Long jobRequestId, String devis);

    Page<JobRequest> getAcceptedRequestsByServiceProvider(Long serviceProviderId, Pageable pageable);
}
