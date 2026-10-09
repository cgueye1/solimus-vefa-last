package com.wakana.vefa.controller.v2;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.dto.v2.CommercialKpiDTO;
import com.wakana.vefa.services.V2.CommercialDashboardService;

@RestController
@RequestMapping("/api/dashboard/commercial")
@RequiredArgsConstructor
public class CommercialDashboardController {

    private final CommercialDashboardService service;

    @GetMapping("/{projectId}/kpi")
    public ResponseEntity<CommercialKpiDTO> getCommercialKpi(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getCommercialKpi(projectId));
    }
}