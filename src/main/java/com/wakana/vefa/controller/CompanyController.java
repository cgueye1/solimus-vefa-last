package com.wakana.vefa.controller;
import com.wakana.vefa.dto.CompanyRequest;
import com.wakana.vefa.model.Company;
import com.wakana.vefa.services.CompanyService;
import com.wakana.vefa.util.FileTraferUtil;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @ModelAttribute CompanyRequest company) {

        try {

            String logo = FileTraferUtil.handleFileUpload(company.getLogo());
            Company updatedCompany = companyService.update(id, company, logo);
            return ResponseEntity.ok(updatedCompany);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Échec de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getById(id);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Company> getCompanyByOwnerId(@PathVariable Long ownerId) {
        Company company = companyService.getByOwnerId(ownerId);
        return ResponseEntity.ok(company);
    }
}
