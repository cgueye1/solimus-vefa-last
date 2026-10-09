package com.wakana.vefa.services;


import com.wakana.vefa.dto.CompanyRequest;
import com.wakana.vefa.model.Company;

public interface CompanyService {
    Company create(Company company);
    Company update(Long id, CompanyRequest company , String logo);
    void delete(Long id);
    Company getById(Long id);
    Company getByOwnerId(Long ownerId);
}
