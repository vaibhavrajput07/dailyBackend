package com.daily.app.backend.controller;

import com.daily.app.backend.dto.dashboard.DashboardResponse;
import com.daily.app.backend.security.AuthPrincipal;
import com.daily.app.backend.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal AuthPrincipal principal) {
        return dashboardService.getTodayDashboard(principal.businessId());
    }
}
