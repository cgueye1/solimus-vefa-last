package com.wakana.vefa.controller;

import com.wakana.vefa.dto.ReservationRequest;
import com.wakana.vefa.dto.ReservationStats;
import com.wakana.vefa.model.Reservation;
import com.wakana.vefa.services.ReservationService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.wakana.vefa.model.User;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // Create a new reservation
    @PostMapping
    @PreAuthorize("hasAuthority('RESERVATAIRE')")
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationRequest reservationRequest) {
        log.info("🚀 API /api/reservations appelée");
        Reservation reservation = reservationService.createReservation(reservationRequest, true);
        return ResponseEntity.ok(reservation);
    }

    // Update an existing reservation
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id,
            @RequestBody ReservationRequest reservationRequest) {
        Reservation updatedReservation = reservationService.updateReservation(id, reservationRequest);
        return ResponseEntity.ok(updatedReservation);
    }

    // Delete a reservation by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    // Get a reservation by its ID
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    // Get all reservations without pagination (ordered by most recent)
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    // Get paginated reservations by reservataire (user) ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Reservation>> getReservationsByUserId(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reservation> reservations = reservationService.getReservationsByUserId(userId, pageable);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('RESERVATAIRE')")
    public ResponseEntity<Page<Reservation>> getMyReservations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(reservationService.getReservationsByUserId(user.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<Page<Reservation>> getReservationsByPropertyId(@PathVariable Long lotId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reservation> reservations = reservationService.getReservationsByLotId(lotId, pageable);
        return ResponseEntity.ok(reservations);
    }

    @PostMapping("/{id}/validation")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public ResponseEntity<?> validateReservation(@PathVariable Long id, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User promoter)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reservationService.validateReservation(id, promoter, true));
    }

    @PostMapping("/{id}/rejection")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public ResponseEntity<Reservation> rejectReservation(@PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        return ResponseEntity.ok(reservationService.rejectReservation(id, (User) authentication.getPrincipal(), reason));
    }

    @PostMapping("/{id}/cancellation")
    @PreAuthorize("hasAuthority('RESERVATAIRE')")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, (User) authentication.getPrincipal()));
    }

    // Get paginated reservations by parent property ID
    @GetMapping("/property/{parentPropertyId}")
    public ResponseEntity<Page<Reservation>> getReservationsByParentPropertyId(@PathVariable Long parentPropertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Reservation> reservations = reservationService.getReservationsByParentProperty(parentPropertyId, pageable);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/dashboard")
    public List<ReservationStats> getReservationStats(
            @RequestParam int year,
            @RequestParam Long promoterId,
            @RequestParam(required = false) Long parentPropertyId,
            @RequestParam String planName) {

        if (parentPropertyId != null) {
            return reservationService.getReservationsByDateAndParentProperty(
                    year, parentPropertyId, promoterId, planName);
        }

        return reservationService.getReservationsByMonth(year, promoterId, planName);
    }

    /*
     * @GetMapping("/dashboard")
     * public List<ReservationStats> getReservationStats(
     * 
     * @RequestParam int year,
     * 
     * @RequestParam Long promoterId,
     * 
     * @RequestParam(required = false) Long parentPropertyId) {
     * 
     * if (parentPropertyId != null) {
     * 
     * return reservationService.getReservationsByDateAndParentProperty(year,
     * parentPropertyId, promoterId);
     * }
     * 
     * return reservationService.getReservationsByMonth(year, promoterId);
     * }
     */

    // 🔹 Reservations LOCATIVES (rental = true)
    @GetMapping("/user/{userId}/rental")
    public Page<Reservation> getRentalReservationsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationService
                .getRentalReservationsByUserId(userId, pageable);
    }

}
