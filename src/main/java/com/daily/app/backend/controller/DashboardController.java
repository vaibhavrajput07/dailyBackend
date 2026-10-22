package com.daily.app.backend.controller;

import com.daily.app.backend.dto.dashboard.ConsolidatedDashboardResponse;
import com.daily.app.backend.dto.dashboard.DashboardResponse;
import com.daily.app.backend.security.AuthPrincipal;
import com.daily.app.backend.security.BusinessScope;
import com.daily.app.backend.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(BusinessScope scope) {
        return dashboardService.getTodayDashboard(scope.businessId());
    }

    @GetMapping("/all")
    public ConsolidatedDashboardResponse getAllBusinessesDashboard(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return dashboardService.getConsolidated(principal.userId(), startDate, endDate);
    }
}
