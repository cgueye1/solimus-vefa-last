package com.wakana.vefa.repository;
import com.wakana.vefa.model.WorkFiles;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface WorkFilesRepository extends JpaRepository<WorkFiles, Long> {
    @Query("SELECT wf FROM WorkFiles wf WHERE wf.work.id = :workId")
    List<WorkFiles> findByWorkId(@Param("workId") Long workId);
}
