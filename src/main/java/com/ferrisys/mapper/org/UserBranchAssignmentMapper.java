package com.ferrisys.mapper.org;

import com.ferrisys.common.dto.org.UserBranchAssignmentDTO;
import com.ferrisys.common.entity.org.UserBranchAssignment;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface UserBranchAssignmentMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    @Mapping(target = "userId", expression = "java(fromUuid(entity.getUserId()))")
    @Mapping(target = "branchId", expression = "java(fromUuid(entity.getBranch().getId()))")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)")
    UserBranchAssignmentDTO toDto(UserBranchAssignment entity);

    List<UserBranchAssignmentDTO> toDtoList(List<UserBranchAssignment> entities);
}
