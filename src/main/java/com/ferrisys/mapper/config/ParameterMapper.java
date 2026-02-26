package com.ferrisys.mapper.config;

import com.ferrisys.common.dto.config.ParameterDTO;
import com.ferrisys.common.entity.config.Parameter;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface ParameterMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    Parameter toEntity(ParameterDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    ParameterDTO toDto(Parameter entity);

    List<ParameterDTO> toDtoList(List<Parameter> entities);
}
