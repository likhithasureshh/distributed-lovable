package com.projects.distributed_lovable.workspace_service.mappers;


import com.projects.distributed_lovable.workspace_service.dto.project.FileNode;
import com.projects.distributed_lovable.workspace_service.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {
    List<FileNode> toFileNodeFromProjectFileList(List<ProjectFile> projectFileList);
}
