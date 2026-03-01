package com.ferrisys.common.dto.org;

public record UserBranchAssignmentEnrichedDTO(
        String id,
        String userId,
        String userFullName,
        String userEmail,
        String branchId,
        String branchCode,
        String branchName,
        Boolean active,
        String updatedAt
) {
}
