package com.wakana.vefa.services.impl;


import com.wakana.vefa.model.RentalInvoice;
import com.wakana.vefa.repository.RentalInvoiceRepository;
import com.wakana.vefa.services.RentalInvoiceService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RentalInvoiceServiceImpl implements RentalInvoiceService {

    private final RentalInvoiceRepository invoiceRepository;

    @Override
    public Page<RentalInvoice> getInvoicesByPropertyAndPaid(Long propertyId, boolean paid, Pageable pageable) {
        return invoiceRepository.findByPropertyIdAndPaidOrderByIdDesc(propertyId, paid, pageable);
    }

    @Override
    public RentalInvoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));
    }

    @Override
    public RentalInvoice createInvoice(RentalInvoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public RentalInvoice updateInvoice(Long id, RentalInvoice invoice) {
        RentalInvoice existingInvoice = getInvoiceById(id);
        existingInvoice.setAmount(invoice.getAmount());
        existingInvoice.setPaid(invoice.isPaid());
        existingInvoice.setDueDate(invoice.getDueDate());
        existingInvoice.setProperty(invoice.getProperty());
        return invoiceRepository.save(existingInvoice);
    }

    @Override
    public void deleteInvoice(Long id) {
        RentalInvoice invoice = getInvoiceById(id);
        invoiceRepository.delete(invoice);
    }
}
