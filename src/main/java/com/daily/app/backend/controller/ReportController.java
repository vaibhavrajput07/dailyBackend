package com.daily.app.backend.controller;

import com.daily.app.backend.dto.report.ReportResponse;
import com.daily.app.backend.security.BusinessScope;
import com.daily.app.backend.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ReportResponse monthly(BusinessScope scope,
                                   @RequestParam int year,
                                   @RequestParam int month) {
        return reportService.getMonthly(scope.businessId(), year, month);
    }

    @GetMapping("/range")
    public ReportResponse range(BusinessScope scope,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.getRange(scope.businessId(), startDate, endDate);
    }
}
