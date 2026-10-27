package com.projects.distributed_lovable.workspace_service.service.impl;


import com.projects.distributed_lovable.common_lib.dto.PlanDto;
import com.projects.distributed_lovable.common_lib.dto.UserDto;
import com.projects.distributed_lovable.common_lib.enums.ProjectRole;
import com.projects.distributed_lovable.common_lib.errors.BadRequestException;
import com.projects.distributed_lovable.common_lib.errors.ResourceNotFoundException;
import com.projects.distributed_lovable.common_lib.security.AuthUtil;
import com.projects.distributed_lovable.workspace_service.clients.AccountClient;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectRequest;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;
import com.projects.distributed_lovable.workspace_service.entity.Project;
import com.projects.distributed_lovable.workspace_service.entity.ProjectMember;
import com.projects.distributed_lovable.workspace_service.entity.ProjectMemberId;
import com.projects.distributed_lovable.workspace_service.mappers.ProjectMapper;
import com.projects.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import com.projects.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.projects.distributed_lovable.workspace_service.service.ProjectService;
import com.projects.distributed_lovable.workspace_service.service.ProjectTemplateService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {
    AccountClient accountClient;
    ProjectRepository projectRepository;
    ProjectMapper projectMapper;
    ProjectMemberRepository projectMemberRepository;
    AuthUtil authUtil;
    ProjectTemplateService projectTemplateService;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        if(!canCreateProject())
        {
            throw new BadRequestException("You cannot create the project with this Plan,Upgrade Now!");
        }
        Long ownerUserId = authUtil.getCurrentUserId();
        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();
        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(),ownerUserId);
        ProjectMember projectMember = ProjectMember
                .builder()
                .id(projectMemberId)
                .projectRole(ProjectRole.OWNER)
                .project(project)
                .invitedAt(Instant.now())
                .acceptedAt(Instant.now())
                .build();
        projectMemberRepository.save(projectMember);
        projectTemplateService.initializeProjectFromTemplate(project.getId());
        return projectMapper.toProjectResponse(project);


    }
    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        Long userId = authUtil.getCurrentUserId();
        var projectWithRoles1 = projectRepository.findAllAccessibleByUser(userId);
        return projectWithRoles1.stream()
                .map(projectWithRoles-> projectMapper.
                        toProjectSummaryResponse(projectWithRoles.getProject(),projectWithRoles.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public ProjectSummaryResponse getUserProjectsById(Long id) {
        Long userId = authUtil.getCurrentUserId();
        var projectWithRole = projectRepository.findAccessibleUserProjectByIdWithRole(id,userId)
                .orElseThrow(()-> new BadRequestException("Project Not Found!"));
        return projectMapper.toProjectSummaryResponse(projectWithRole.getProject(),projectWithRole.getRole());
    }



    @Override
    public ProjectResponse updateProjectById(Long id, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleUserProjectById(id,userId);
        project.setName(request.name());
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public void softDelete(Long id)
    {
        Long userId = authUtil.getCurrentUserId();
       Project project = getAccessibleUserProjectById(id, userId);
       project.setDeletedAt(Instant.now());
       projectRepository.save(project);
    }

    public Project getAccessibleUserProjectById(Long projectId,Long userId)
    {
        return projectRepository.findAccessibleUserProjectById(projectId,userId)
                .orElseThrow(()-> new ResourceNotFoundException("project",projectId.toString()));
    }
    private boolean canCreateProject() {
        Long userId = authUtil.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        PlanDto plan = accountClient.getCurrentSubscribedPlanByUser();

        int maxAllowed = plan.maxProjects();
        int ownedCount = projectMemberRepository.findUserOwnedProjects(userId);

        return ownedCount < maxAllowed;
    }
}
