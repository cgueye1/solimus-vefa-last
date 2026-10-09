package com.wakana.vefa.services.V2;

import com.wakana.vefa.dto.v2.CommercialKpiDTO;

// CommercialDashboardService.java

public interface CommercialDashboardService {
    CommercialKpiDTO getCommercialKpi(Long projectId);
}