package com.projects.distributed_lovable.workspace_service.controller;

import com.projects.distributed_lovable.workspace_service.dto.project.ProjectRequest;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.projects.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;
import com.projects.distributed_lovable.workspace_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    //private final DeploymentService deploymentService;
    @GetMapping
    public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects()
    {
        return ResponseEntity.ok(projectService.getUserProjects());
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("@security.canViewProject(#id)")
    public ResponseEntity<ProjectSummaryResponse> getProjectById(@PathVariable Long id)
    {
        return ResponseEntity.ok(projectService.getUserProjectsById(id));
    }

    @PostMapping

    public ResponseEntity<ProjectResponse> createProject(@RequestBody @Valid ProjectRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PatchMapping(path = "/{id}")
    @PreAuthorize("@security.canEditProject(#id)")
    public ResponseEntity<ProjectResponse> updateProjectById(@PathVariable Long id,
                                                             @RequestBody @Valid ProjectRequest request)
    {
        return ResponseEntity.ok(projectService.updateProjectById(id,request));
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("@security.canDeleteProject(#id)")
    public ResponseEntity<Void> deleteProjectById(@PathVariable Long id)
    {
        projectService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/{id}/deploy")
//    public ResponseEntity<DeployResponse> deployProject(@PathVariable Long id)
//    {
//        return ResponseEntity.ok(deploymentService.deploy(id));
//    }
}
