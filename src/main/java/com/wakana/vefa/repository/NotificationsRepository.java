package com.wakana.vefa.repository;
import com.wakana.vefa.model.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;


public interface NotificationsRepository extends JpaRepository<Notifications, Long> {

    @Query("SELECT n FROM Notifications n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    Page<Notifications> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
