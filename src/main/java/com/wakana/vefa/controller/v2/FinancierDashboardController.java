package com.wakana.vefa.controller.v2;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.dto.v2.EncaissementDetailDTO;
import com.wakana.vefa.dto.v2.FinancierKpiDTO;
import com.wakana.vefa.services.V2.FinancierDashboardService;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/financier")
@RequiredArgsConstructor
public class FinancierDashboardController {

    private final FinancierDashboardService service;

    @GetMapping("/{projectId}/kpi")
    public ResponseEntity<FinancierKpiDTO> getFinancierKpi(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getFinancierKpi(projectId));
    }

    @GetMapping("/{projectId}/echeancier")
    public ResponseEntity<List<EncaissementDetailDTO>> getEcheancier(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getEcheancierDetail(projectId));
    }
}