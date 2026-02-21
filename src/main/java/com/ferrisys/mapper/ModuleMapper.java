package com.ferrisys.mapper;

import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.mapper.support.IdMappingSupport;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface ModuleMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    AuthModule toEntity(ModuleDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    ModuleDTO toDto(AuthModule entity);

    List<ModuleDTO> toDtoList(List<AuthModule> entities);
}
