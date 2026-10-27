package com.projects.distributed_lovable.workspace_service.mappers;


import com.projects.distributed_lovable.common_lib.enums.ProjectRole;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;
import com.projects.distributed_lovable.workspace_service.entity.Project;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectResponse toProjectResponse(Project project);


    ProjectSummaryResponse toProjectSummaryResponse(Project project, ProjectRole role);

    List<ProjectSummaryResponse> toProjectSummaryResponse(List<Project> projectList);
}
