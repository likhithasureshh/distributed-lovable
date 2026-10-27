package com.projects.distributed_lovable.account_service.mapper;


import com.projects.distributed_lovable.account_service.dto.auth.SignupRequest;
import com.projects.distributed_lovable.account_service.dto.auth.UserProfileResponse;
import com.projects.distributed_lovable.account_service.entity.User;
import com.projects.distributed_lovable.common_lib.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUserFromSignUpRequest(SignupRequest signupRequest);
    UserProfileResponse toUserProfileResponse(User user);
    UserDto toUserDto(User user);
}
