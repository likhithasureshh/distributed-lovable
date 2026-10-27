package com.projects.distributed_lovable.account_service.dto.subscription;

public record PlanLimitsResponse(
        String planName,
        Integer maxProjects,
        Integer maxTokensPerDay,
        Boolean unlimitedAi


) {
}
