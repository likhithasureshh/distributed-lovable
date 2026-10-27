package com.projects.distributed_lovable.workspace_service.mappers;


import com.projects.distributed_lovable.workspace_service.dto.members.MemberResponse;
import com.projects.distributed_lovable.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {
    @Mapping(target = "userId",source = "id")
    @Mapping(target = "role",constant = "OWNER")
    //MemberResponse toMemberResponseFromUser(User user);

    @Mapping(target = "userId",source = "user.id")
    @Mapping(target = "username",source = "user.username")
    @Mapping(target = "name",source = "user.name")
    @Mapping(target = "role",source = "projectRole")
    MemberResponse toMemberResponseFromProjectMember(ProjectMember projectMember);
}
