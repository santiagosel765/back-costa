package com.ferrisys.mapper.config;

import com.ferrisys.common.dto.config.TaxDTO;
import com.ferrisys.common.entity.config.Tax;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface TaxMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    Tax toEntity(TaxDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)")
    TaxDTO toDto(Tax entity);

    List<TaxDTO> toDtoList(List<Tax> entities);
}
