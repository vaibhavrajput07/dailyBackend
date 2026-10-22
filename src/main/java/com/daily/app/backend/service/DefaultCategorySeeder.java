package com.daily.app.backend.service;

import com.daily.app.backend.entity.BusinessType;

import java.util.List;
import java.util.Map;

public final class DefaultCategorySeeder {

    private static final List<String> COMMON = List.of("Rent", "Electricity", "Staff Salary");

    private static final Map<BusinessType, List<String>> BY_TYPE = Map.of(
            BusinessType.MEDICAL_STORE, List.of("Medicine Purchase", "Transport"),
            BusinessType.GROCERY, List.of("Stock Purchase", "Transport"),
            BusinessType.SALON, List.of("Supplies", "Maintenance"),
            BusinessType.RESTAURANT, List.of("Raw Material", "Gas & Fuel"),
            BusinessType.BAKERY, List.of("Raw Material", "Packaging"),
            BusinessType.CLINIC, List.of("Medical Supplies", "Equipment"),
            BusinessType.GARAGE, List.of("Spare Parts", "Tools"),
            BusinessType.HARDWARE_SHOP, List.of("Stock Purchase", "Transport"),
            BusinessType.OTHER, List.of("Stock Purchase", "Transport")
    );

    private DefaultCategorySeeder() {
    }

    public static List<String> defaultsFor(BusinessType businessType) {
        return java.util.stream.Stream.concat(COMMON.stream(), BY_TYPE.get(businessType).stream())
                .toList();
    }
}
