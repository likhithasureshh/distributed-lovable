package com.projects.distributed_lovable.workspace_service.entity;

import java.time.Instant;

public class Preview {
    Long id;
    Project project;
    String namespace;
    String podName;
    String previewUrl;
    Instant startedAt;
    Instant terminatedAt;
    Instant createdAt;
}
