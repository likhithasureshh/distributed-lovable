package com.projects.distributed_lovable.workspace_service.controller;

import com.projects.distributed_lovable.workspace_service.dto.members.InviteMemberRequest;
import com.projects.distributed_lovable.workspace_service.dto.members.MemberResponse;
import com.projects.distributed_lovable.workspace_service.dto.members.UpdateMemberRoleRequest;
import com.projects.distributed_lovable.workspace_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @GetMapping
    @PreAuthorize("@security.canViewMembers(#projectId)")
    public ResponseEntity<List<MemberResponse>> getProjectMembers(@PathVariable Long projectId)
    {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }

    @PostMapping
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public ResponseEntity<MemberResponse> inviteProjectMember(@PathVariable Long projectId,
                                                              @RequestBody @Valid InviteMemberRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(projectMemberService.inviteProjectMember(projectId,request));

    }

    @PatchMapping("/{memberId}")
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public ResponseEntity<MemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestBody @Valid UpdateMemberRoleRequest request
    )
    {
        return ResponseEntity.ok(projectMemberService.updateMemberRole(projectId,memberId,request));
    }


    @DeleteMapping("/{memberId}")
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public ResponseEntity<Void> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId
    )
    {
        projectMemberService.removeProjectMember(projectId,memberId);
        return ResponseEntity.noContent().build();
    }

}
