package com.projects.distributed_lovable.common_lib.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.projects.distributed_lovable.common_lib.enums.ProjectPermissions.*;


@RequiredArgsConstructor
@Getter
public enum ProjectRole {
    EDITOR(Set.of(VIEW,EDIT,DELETE,VIEW_MEMBERS)),
    VIEWER(Set.of(VIEW,VIEW_MEMBERS)),
    OWNER(Set.of(VIEW,EDIT,DELETE,MANAGE_MEMBERS,VIEW_MEMBERS));
    private final Set<ProjectPermissions> permissionsSet;
}
