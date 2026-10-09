package com.wakana.vefa.controller;
import com.wakana.vefa.model.RentalInvoice;
import com.wakana.vefa.services.RentalInvoiceService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rental-invoices")
@RequiredArgsConstructor
public class RentalInvoiceController {

    private final RentalInvoiceService invoiceService;

    // Liste des factures par bien et état de paiement
    @GetMapping
    public ResponseEntity<Page<RentalInvoice>> getInvoicesByPropertyAndPaid(
            @RequestParam Long propertyId,
            @RequestParam boolean paid,
            Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getInvoicesByPropertyAndPaid(propertyId, paid, pageable));
    }

    // Récupérer une facture par ID
    @GetMapping("/{id}")
    public ResponseEntity<RentalInvoice> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    // Créer une nouvelle facture
    @PostMapping
    public ResponseEntity<RentalInvoice> createInvoice(@RequestBody RentalInvoice invoice) {
        return ResponseEntity.ok(invoiceService.createInvoice(invoice));
    }

    // Mettre à jour une facture
    @PutMapping("/{id}")
    public ResponseEntity<RentalInvoice> updateInvoice(@PathVariable Long id, @RequestBody RentalInvoice invoice) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, invoice));
    }

    // Supprimer une facture
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}
