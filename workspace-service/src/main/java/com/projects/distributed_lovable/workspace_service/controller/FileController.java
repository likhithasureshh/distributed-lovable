package com.projects.distributed_lovable.workspace_service.controller;


import com.projects.distributed_lovable.workspace_service.dto.project.FileContentResponse;
import com.projects.distributed_lovable.workspace_service.dto.project.FileTreeResponse;
import com.projects.distributed_lovable.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/files")
public class FileController {
    private final ProjectFileService projectFileService;

    @GetMapping
    public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable Long projectId)
    {
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping(path = "content")
    public ResponseEntity<FileContentResponse> getFile(
            @PathVariable Long projectId,
            @RequestParam String path
    )
    {
        return ResponseEntity.ok(projectFileService.getFileContent(projectId,path));
    }
}
