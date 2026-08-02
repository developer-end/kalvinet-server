package app.school.administration.dashboard.api.controller;

import app.school.administration.dashboard.api.response.DashboardConfigDTO;
import app.school.administration.dashboard.application.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ====================================================================================
 * CONTROLLER: DashboardController
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * REST controller exposing dashboard configuration and live aggregated metrics for KalviNet ERP.
 * Endpoint: `GET /api/v1/dashboard/config`
 * ====================================================================================
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/config")
    public ResponseEntity<DashboardConfigDTO> getDashboardConfig() {
        DashboardConfigDTO config = dashboardService.getDashboardConfig();
        return ResponseEntity.ok(config);
    }
}
