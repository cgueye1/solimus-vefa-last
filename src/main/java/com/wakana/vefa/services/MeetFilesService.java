package com.wakana.vefa.services;

import java.util.List;

import com.wakana.vefa.dto.MeetFileRequest;
import com.wakana.vefa.model.MeetFiles;

public interface MeetFilesService {
    public List<MeetFiles> getFilesByMeetId(Long meetId);

    public MeetFiles save(MeetFileRequest request);

}
