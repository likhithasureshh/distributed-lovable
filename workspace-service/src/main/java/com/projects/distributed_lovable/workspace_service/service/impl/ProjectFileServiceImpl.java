package com.projects.distributed_lovable.workspace_service.service.impl;


import com.projects.distributed_lovable.common_lib.errors.ResourceNotFoundException;
import com.projects.distributed_lovable.workspace_service.dto.project.FileContentResponse;
import com.projects.distributed_lovable.workspace_service.dto.project.FileNode;
import com.projects.distributed_lovable.workspace_service.dto.project.FileTreeResponse;
import com.projects.distributed_lovable.workspace_service.entity.Project;
import com.projects.distributed_lovable.workspace_service.entity.ProjectFile;
import com.projects.distributed_lovable.workspace_service.mappers.ProjectFileMapper;
import com.projects.distributed_lovable.workspace_service.repository.ProjectFileRepository;
import com.projects.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.projects.distributed_lovable.workspace_service.service.ProjectFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectFileServiceImpl implements ProjectFileService {
    private final ProjectRepository projectRepository;
    private final MinioClient minioClient;
    private final ProjectFileRepository projectFileRepository;
    private final ProjectFileMapper projectFileMapper;
    private static final String BUCKET_NAME = "projects";

    @Value("${minio.project-bucket}")
    private String projectBucket;
    @Override
    public FileContentResponse getFileContent(Long projectId, String path)
    {
        String objectName = projectId + "/" + path;
        try (
                InputStream is = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(objectName)
                                .build())) {

            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new FileContentResponse(path, content);
        } catch (Exception e) {
            log.error("Failed to read file: {}/{}", projectId, path, e);
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    @Override
    public FileTreeResponse getFileTree(Long projectId) {
        List<ProjectFile> projectFileList = projectFileRepository.findByProjectId(projectId);
        List<FileNode> projectFileNode =  projectFileMapper.toFileNodeFromProjectFileList(projectFileList);
        return new FileTreeResponse(projectFileNode);
    }

    @Override
    public void saveFiles(Long projectId, String path, String fileContent) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new ResourceNotFoundException("project",projectId.toString()));

        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        String objectKey = projectId + "/" + cleanPath;
        try {
            byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);
            // saving the file content
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectKey)
                            .stream(inputStream, contentBytes.length, -1)
                            .contentType(determineContentType(path))
                            .build());

            // Saving the metaData
            ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, cleanPath)
                    .orElseGet(() -> ProjectFile.builder()
                            .project(project)
                            .path(cleanPath)
                            .minioObjectKey(objectKey) // Use the key we generated
                            .createdAt(Instant.now())
                            .build());

            file.setUpdatedAt(Instant.now());
            projectFileRepository.save(file);
            log.info("Saved file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to save file {}/{}", projectId, cleanPath, e);
            throw new RuntimeException("File save failed", e);
        }


    }
    private String determineContentType(String path) {
        String type = URLConnection.guessContentTypeFromName(path);
        if (type != null) return type;
        if (path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx")) return "text/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".css")) return "text/css";

        return "text/plain";
    }
}
