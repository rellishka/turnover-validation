package com.turnover.validation.adapter.out.tenantapi;

import java.util.List;

/** A page of the Tenant App's turnover response. */
public record TenantApiPage(int page, int totalPages, List<TenantApiTurnover> items) {
}
