package com.projects.distributed_lovable.workspace_service.service;


import com.projects.distributed_lovable.workspace_service.dto.project.FileContentResponse;
import com.projects.distributed_lovable.workspace_service.dto.project.FileTreeResponse;

public interface ProjectFileService {

    FileContentResponse getFileContent(Long projectId, String path);

    FileTreeResponse getFileTree(Long projectId);

    void saveFiles(Long projectId, String filePath, String fileContent);
}
