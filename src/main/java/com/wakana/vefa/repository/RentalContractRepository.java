package com.wakana.vefa.repository;

import com.wakana.vefa.model.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {

    List<RentalContract> findByArchivedFalse();

    List<RentalContract> findByArchivedTrue();

    List<RentalContract> findByRecipientId(Long recipientId);

    // ✅ Contrats actifs d’un locataire
    List<RentalContract> findByRecipientIdAndArchivedFalse(Long recipientId);

    // ✅ Contrats archivés d’un bien
    List<RentalContract> findByPropertyIdAndArchivedTrue(Long propertyId);
}
