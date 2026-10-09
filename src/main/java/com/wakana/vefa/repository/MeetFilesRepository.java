package com.wakana.vefa.repository;

import com.wakana.vefa.model.MeetFiles;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface MeetFilesRepository extends JpaRepository<MeetFiles, Long> {

    @Query("SELECT mf FROM MeetFiles mf WHERE mf.meet.id = :meetId")
    List<MeetFiles> findByMeetId(@Param("meetId") Long meetId);
}
