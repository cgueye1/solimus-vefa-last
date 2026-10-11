package com.wakana.vefa.services;

import com.wakana.vefa.dto.ReservationRequest;
import com.wakana.vefa.dto.ReservationStats;
import com.wakana.vefa.model.Reservation;
import com.wakana.vefa.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReservationService {
    Reservation createReservation(ReservationRequest reservationRequest, boolean sendNotif);

    Reservation updateReservation(Long id, ReservationRequest reservationRequest);

    void deleteReservation(Long id);

    Reservation getReservationById(Long id);

    List<Reservation> getAllReservations();

    Page<Reservation> getReservationsByParentProperty(Long parentPropertyId, Pageable pageable);

    Page<Reservation> getReservationsByUserId(Long userId, Pageable pageable);

    Page<Reservation> getReservationsByLotId(Long lotId, Pageable pageable);

    Reservation valideReservation(Long id, boolean canSendMail);

        Reservation validateReservation(Long id, User promoter, boolean canSendMail);

        Reservation rejectReservation(Long id, User promoter, String reason);

        Reservation cancelReservation(Long id, User reservataire);

    Page<Reservation> getRentalReservationsByUserId(Long userId, Pageable pageable);

    /// promoter dash
    // public List<ReservationStats> getReservationsByMonth(int year, Long
    /// promoterId) ;
    /*
     * List<ReservationStats> getReservationsByDateAndParentProperty(
     * int year,
     * Long parentPropertyId,
     * Long promoterId);
     */

    public List<ReservationStats> getReservationsByMonth(
            int year,
            Long promoterId,
            String planName);

    List<ReservationStats> getReservationsByDateAndParentProperty(
            int year,
            Long parentPropertyId,
            Long promoterId,
            String planName);

}
