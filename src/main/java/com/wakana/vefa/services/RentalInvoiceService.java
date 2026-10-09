package com.wakana.vefa.services;
import com.wakana.vefa.model.RentalInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalInvoiceService {
    // Liste des factures par bien et état de paiement, triées et paginées
    Page<RentalInvoice> getInvoicesByPropertyAndPaid(Long propertyId, boolean paid, Pageable pageable);
    // CRUD
    RentalInvoice getInvoiceById(Long id);
    RentalInvoice createInvoice(RentalInvoice invoice);
    RentalInvoice updateInvoice(Long id, RentalInvoice invoice);
    void deleteInvoice(Long id);
}
