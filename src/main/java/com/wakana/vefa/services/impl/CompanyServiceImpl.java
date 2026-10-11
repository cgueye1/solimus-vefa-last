package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.CompanyRequest;
import com.wakana.vefa.model.Company;
import com.wakana.vefa.repository.CompanyRepository;
import com.wakana.vefa.services.CompanyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public Company create(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public Company update(Long id, CompanyRequest company, String logo) {
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
        existingCompany.setName(company.getName());
        existingCompany.setAddress(company.getAddress());
        existingCompany.setRegistrationNumber(company.getRegistrationNumber());
        existingCompany.setNinea(company.getNinea());
        existingCompany.setPrimaryColor(company.getPrimaryColor());
        existingCompany.setSecondaryColor(company.getSecondaryColor());
        if (logo != null && !logo.isEmpty()) {
            existingCompany.setLogo(logo);
        }
        return companyRepository.save(existingCompany);
    }

    @Override
    public void delete(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
        companyRepository.delete(company);
    }

    @Override
    public Company getById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
    }

    @Override
    public Company getByOwnerId(Long ownerId) {
        return companyRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found for owner id: " + ownerId));
    }
}
