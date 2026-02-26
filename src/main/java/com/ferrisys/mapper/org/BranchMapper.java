package com.ferrisys.mapper.org;

import com.ferrisys.common.dto.org.BranchDTO;
import com.ferrisys.common.entity.org.Branch;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface BranchMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    Branch toEntity(BranchDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    BranchDTO toDto(Branch entity);

    List<BranchDTO> toDtoList(List<Branch> entities);
}
