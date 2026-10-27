package com.projects.distributed_lovable.workspace_service.dto.members;

import com.project.lovable_clone.enums.ProjectRole;

public record MemberResponse(
        Long userId,
        String name,
        String username,
        String avatarUrl,
        String invitedAt,
        ProjectRole role
) {
}
