package com.projects.distributed_lovable.account_service.dto.auth;

public record UserProfileResponse (
        Long id,
        String name,
        String username,
        String avatarUrl
){
}
