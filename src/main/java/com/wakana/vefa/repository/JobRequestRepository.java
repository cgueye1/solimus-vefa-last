package com.wakana.vefa.repository;



import com.wakana.vefa.enums.RequestStateEnum;
import com.wakana.vefa.model.JobRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRequestRepository extends JpaRepository<JobRequest, Long> {
    
    Page<JobRequest> findAllByRequesterIdAndState(Long requesterId, RequestStateEnum state, Pageable pageable);
    Page<JobRequest> findAllByServiceProviderIdAndState(Long serviceProviderId, RequestStateEnum state, Pageable pageable);


    @Query("SELECT jr FROM JobRequest jr WHERE jr.state = :state AND jr.job.id = (SELECT sp.job.id FROM User sp WHERE sp.id = :serviceProviderId)")
    Page<JobRequest>findPendingRequestsByServiceProviderDomain(Long serviceProviderId, RequestStateEnum state, Pageable pageable);
  

    @Query("SELECT jr FROM JobRequest jr WHERE jr.state = :state AND jr.serviceProvider.id = :serviceProviderId")
    Page<JobRequest> findAcceptedRequestsByServiceProvider(Long serviceProviderId, RequestStateEnum state,Pageable pageable);
}
