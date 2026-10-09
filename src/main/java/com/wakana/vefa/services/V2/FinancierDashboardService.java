package com.wakana.vefa.services.V2;

import java.util.List;

import com.wakana.vefa.dto.v2.EncaissementDetailDTO;
import com.wakana.vefa.dto.v2.FinancierKpiDTO;

public interface FinancierDashboardService {
    FinancierKpiDTO getFinancierKpi(Long projectId);

    List<EncaissementDetailDTO> getEcheancierDetail(Long projectId);
}