package com.projects.distributed_lovable.workspace_service.service;



import com.projects.distributed_lovable.workspace_service.dto.members.InviteMemberRequest;
import com.projects.distributed_lovable.workspace_service.dto.members.MemberResponse;
import com.projects.distributed_lovable.workspace_service.dto.members.UpdateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService {
    List<MemberResponse> getProjectMembers(Long projectId);

    MemberResponse inviteProjectMember(Long projectId, InviteMemberRequest request);

    MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request);

    void removeProjectMember(Long projectId, Long memberId);
}
