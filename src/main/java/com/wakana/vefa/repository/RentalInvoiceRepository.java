package com.wakana.vefa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wakana.vefa.model.RentalInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalInvoiceRepository extends JpaRepository<RentalInvoice, Long> {

    Page<RentalInvoice> findByPropertyIdAndPaidOrderByIdDesc(Long propertyId, boolean paid, Pageable pageable);

}