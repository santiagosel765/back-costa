package com.ferrisys.mapper.config;

import com.ferrisys.common.dto.config.CurrencyDTO;
import com.ferrisys.common.entity.config.Currency;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface CurrencyMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    Currency toEntity(CurrencyDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)")
    CurrencyDTO toDto(Currency entity);

    List<CurrencyDTO> toDtoList(List<Currency> entities);
}
