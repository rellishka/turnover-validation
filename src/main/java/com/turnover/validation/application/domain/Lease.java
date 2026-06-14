package com.turnover.validation.application.domain;

import java.time.LocalDate;

public record Lease(Long id, Property property, Tenant tenant, LocalDate startDate, LocalDate endDate) {
}
