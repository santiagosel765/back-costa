package com.ferrisys.mapper.org;

import com.ferrisys.common.dto.org.WarehouseDTO;
import com.ferrisys.common.entity.org.Warehouse;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface WarehouseMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    @Mapping(target = "branchId", expression = "java(fromUuid(entity.getBranch().getId()))")
    WarehouseDTO toDto(Warehouse entity);

    List<WarehouseDTO> toDtoList(List<Warehouse> entities);
}
