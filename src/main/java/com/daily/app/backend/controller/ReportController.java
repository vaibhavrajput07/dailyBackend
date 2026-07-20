package com.daily.app.backend.controller;

import com.daily.app.backend.dto.report.ReportResponse;
import com.daily.app.backend.security.AuthPrincipal;
import com.daily.app.backend.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ReportResponse monthly(@AuthenticationPrincipal AuthPrincipal principal,
                                   @RequestParam int year,
                                   @RequestParam int month) {
        return reportService.getMonthly(principal.businessId(), year, month);
    }

    @GetMapping("/range")
    public ReportResponse range(@AuthenticationPrincipal AuthPrincipal principal,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.getRange(principal.businessId(), startDate, endDate);
    }
}
