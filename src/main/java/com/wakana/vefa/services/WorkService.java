package com.wakana.vefa.services;

import com.wakana.vefa.dto.MeetFileRequest;
import com.wakana.vefa.dto.WorkRequest;
import com.wakana.vefa.dto.response.RealEstatePropertyKpiResponse;
import com.wakana.vefa.dto.response.WorkKpiResponse;
import com.wakana.vefa.model.MeetFiles;
import com.wakana.vefa.model.Work;
import com.wakana.vefa.model.WorkFiles;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface WorkService {
    Work createWork(Long propertyId, WorkRequest request);

    Work updateWork(Long id, WorkRequest request);

    Optional<Work> getworkDetail(Long workId);

    void deleteWork(Long id);

    Page<Work> listWorksByProperty(Long propertyId, String month, Pageable pageable);

    Work changeStatus(Long id, String status);

    Work addPictures(Long workId, List<MultipartFile> files);

    Work removePicture(Long workId, String pictureUrl);

    WorkKpiResponse getKpiByWork(Long workId);

    RealEstatePropertyKpiResponse getKpi(Long propertyId, String month);

    List<WorkFiles> getFilesByWorkId(Long workId);

    WorkFiles addFile(MeetFileRequest request);
}
