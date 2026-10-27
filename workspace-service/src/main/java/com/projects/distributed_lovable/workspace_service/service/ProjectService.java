package com.projects.distributed_lovable.workspace_service.service;



import com.projects.distributed_lovable.workspace_service.dto.project.ProjectRequest;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectSummaryResponse> getUserProjects();

    ProjectSummaryResponse getUserProjectsById(Long id);

    ProjectResponse createProject(ProjectRequest request);

    ProjectResponse updateProjectById(Long id, ProjectRequest request);


    void softDelete(Long id);
}
