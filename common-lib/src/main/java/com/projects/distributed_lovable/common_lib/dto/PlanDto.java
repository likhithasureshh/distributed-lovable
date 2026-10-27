package com.projects.distributed_lovable.common_lib.dto;

public record PlanDto(
        Long id,
        String name,
        Integer maxProjects,
        Long maxTokensPerDay,
        Boolean unlimitedAi,
        String price
) {
}
