package com.projects.distributed_lovable.workspace_service.service.impl;


import com.projects.distributed_lovable.common_lib.dto.UserDto;
import com.projects.distributed_lovable.common_lib.errors.ResourceNotFoundException;
import com.projects.distributed_lovable.common_lib.security.AuthUtil;
import com.projects.distributed_lovable.workspace_service.clients.AccountClient;
import com.projects.distributed_lovable.workspace_service.dto.members.InviteMemberRequest;
import com.projects.distributed_lovable.workspace_service.dto.members.MemberResponse;
import com.projects.distributed_lovable.workspace_service.dto.members.UpdateMemberRoleRequest;
import com.projects.distributed_lovable.workspace_service.entity.Project;
import com.projects.distributed_lovable.workspace_service.entity.ProjectMember;
import com.projects.distributed_lovable.workspace_service.entity.ProjectMemberId;
import com.projects.distributed_lovable.workspace_service.mappers.ProjectMemberMapper;
import com.projects.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import com.projects.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.projects.distributed_lovable.workspace_service.service.ProjectMemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {
    ProjectMemberRepository projectMemberRepository;
    ProjectRepository projectRepository;
    ProjectMemberMapper projectMemberMapper;
    AccountClient accountClient;
    AuthUtil authUtil;
    @Override
    public List<MemberResponse> getProjectMembers(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleUserProjectById(projectId,userId);

        return projectMemberRepository.findByIdProjectId(projectId).stream()
                        .map(projectMemberMapper::toMemberResponseFromProjectMember)
                        .collect(Collectors.toList());
    }

    @Override
    public MemberResponse inviteProjectMember(Long projectId, InviteMemberRequest request)
    {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleUserProjectById(projectId,userId);
        UserDto invitee = accountClient.getUserByEmail(request.username())
                .orElseThrow(()-> new ResourceNotFoundException("user", request.username()));
        if(invitee.id().equals(userId))
        {
            throw new RuntimeException("You cannot invite yourself!");
        }
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,invitee.id());
        if(projectMemberRepository.existsById(projectMemberId))
        {
            throw new RuntimeException("You are already a Project Member!");
        }
        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .projectRole(request.role())
                .invitedAt(Instant.now())
                .project(project)
                .build();
        projectMemberRepository.save(projectMember);
        return projectMemberMapper.toMemberResponseFromProjectMember(projectMember);
    }

    @Override
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request)
    {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleUserProjectById(projectId,userId);
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();
        projectMember.setProjectRole(request.role());
        projectMember=projectMemberRepository.save(projectMember);

        return projectMemberMapper.toMemberResponseFromProjectMember(projectMember);
    }

    @Override
    public void removeProjectMember(Long projectId, Long memberId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleUserProjectById(projectId,userId);
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,memberId);
        projectMemberRepository.deleteById(projectMemberId);
    }

    public Project getAccessibleUserProjectById(Long projectId, Long userId)
    {
        return projectRepository.findAccessibleUserProjectById(projectId,userId)
                .orElseThrow(()-> new ResourceNotFoundException("project",projectId.toString()));
    }
}
