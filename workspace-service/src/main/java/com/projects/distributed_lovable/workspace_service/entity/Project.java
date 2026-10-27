package com.projects.distributed_lovable.workspace_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "projects",
        indexes =
                {
                        @Index(name = "idx_projects_deleted_updated",columnList = "deleted_at,updated_at"),
                        @Index(name = "idx_projects_deleted",columnList = "deleted_at"),
                        @Index(name = "idx_projects_updated_deleted",columnList = "updated_at,deleted_at")
                }
)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String name;
    Boolean isPublic = false;
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;
    Instant deletedAt;
}
