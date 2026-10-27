package com.projects.distributed_lovable.workspace_service.repository;


import com.projects.distributed_lovable.common_lib.enums.ProjectRole;
import com.projects.distributed_lovable.workspace_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {
    @Query("""
    SELECT p as project,pm.projectRole as role
    FROM Project p
    JOIN ProjectMember pm ON pm.project.id = p.id
    WHERE pm.id.userId=:userId
    AND p.deletedAt IS NULL
    ORDER BY p.updatedAt DESC
    """)
    List<ProjectWithRole> findAllAccessibleByUser(@Param("userId") Long userId);

    @Query("""
     SELECT p from Project p
     WHERE p.deletedAt is NULL
     AND EXISTS(
        SELECT 1 FROM ProjectMember pm
        WHERE pm.id.userId =:userId
        AND pm.id.projectId =:projectId
          )   
     AND p.id =:projectId
     """)
    Optional<Project> findAccessibleUserProjectById(@Param("projectId") Long projectId, @Param("userId") Long userId);


    @Query("""
     SELECT p as project,pm.projectRole as role
     FROM Project p
     JOIN ProjectMember pm ON pm.project.id = p.id
     WHERE p.id = :projectId
     AND pm.id.userId =:userId
     AND deletedAt is NULL
     """)
    Optional<ProjectWithRole> findAccessibleUserProjectByIdWithRole(@Param("projectId") Long projectId, @Param("userId") Long userId);

     interface ProjectWithRole{
         Project getProject();
         ProjectRole getRole();
     }
}
