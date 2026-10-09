package com.wakana.vefa.services.impl;

import java.util.List;


import org.springframework.stereotype.Service;

import com.wakana.vefa.dto.MeetFileRequest;
import com.wakana.vefa.model.Meet;
import com.wakana.vefa.model.MeetFiles;
import com.wakana.vefa.repository.MeetFilesRepository;
import com.wakana.vefa.repository.MeetRepository;
import com.wakana.vefa.services.MeetFilesService;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetFilesServiceImpl implements MeetFilesService{
    
        private final MeetFilesRepository meetFilesRepository;
        private  final MeetRepository meetRepository;

    
        public List<MeetFiles> getFilesByMeetId(Long meetId) {
            return meetFilesRepository.findByMeetId(meetId);
        }


        @Override
        public MeetFiles save(MeetFileRequest request) {
             Meet meet = meetRepository.findById(request.getMeetId())
                .orElseThrow(() -> new IllegalArgumentException("Meet not found with id: " + request.getMeetId()));
          MeetFiles metFile = new MeetFiles();
          metFile.setFile(request.getFileName());
          metFile.setLabel(request.getLabel());
          metFile.setMeet(meet);
          return meetFilesRepository.save(metFile);
        }
   

}
