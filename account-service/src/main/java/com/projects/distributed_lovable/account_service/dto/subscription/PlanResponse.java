package com.projects.distributed_lovable.account_service.dto.subscription;

public record PlanResponse(
    Long id,
    String name,
    Long maxProjects,
    Long maxTokensPerDay,
    Boolean unlimitedAi,
    String price
) {
}
