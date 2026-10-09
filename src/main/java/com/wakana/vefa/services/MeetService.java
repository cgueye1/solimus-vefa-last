package com.wakana.vefa.services;

import java.time.LocalDateTime;
import java.util.List;

import com.wakana.vefa.dto.MeetRequest;
import com.wakana.vefa.dto.ProgressAlbumRequest;
import com.wakana.vefa.dto.response.MeetStatsResponse;
import com.wakana.vefa.model.Meet;
import com.wakana.vefa.model.ProgressAlbum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetService {

    Meet createMeet(MeetRequest meetRequest);

    public Meet createMeetCOP(MeetRequest meetRequest);

    Meet updateMeet(Long id, MeetRequest meetRequest);

    void deleteMeet(Long id);

    List<Meet> getAllMeets();

    Meet getMeetById(Long id);

    void addParticipants(Long meetId, List<Long> participantIds);

    boolean isOverlapping(LocalDateTime dateTime, int duration);

    Page<Meet> getMeetsByCreator(Long creatorId, int month, int year, Pageable pageable);

    Page<Meet> getMeetsByParticipant(Long userId, int month, int year, Pageable pageable);

    Page<Meet> getUpcomingMeets(Long realEstatePropertyId, Pageable pageable);

    Page<Meet> getPastMeets(Long realEstatePropertyId, Pageable pageable);

    Meet addFiles(Long id, List<String> pictureUrls);

    MeetStatsResponse getMeetStats(Long propertyId);

}
