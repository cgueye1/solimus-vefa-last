
package com.wakana.vefa.services;


import org.springframework.data.domain.Page;

import com.wakana.vefa.dto.RapportRequest;
import com.wakana.vefa.model.Rapport;


public interface RapportService {
    Rapport save(RapportRequest  request, String pdf);
    void delete(Long id);
    Page<Rapport>  getAll(int page, int size,Long id);
     Rapport toggleArchive(Long rapportId);

}
