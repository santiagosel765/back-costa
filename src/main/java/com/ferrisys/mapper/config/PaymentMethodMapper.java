package com.ferrisys.mapper.config;

import com.ferrisys.common.dto.config.PaymentMethodDTO;
import com.ferrisys.common.entity.config.PaymentMethod;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface PaymentMethodMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    PaymentMethod toEntity(PaymentMethodDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    PaymentMethodDTO toDto(PaymentMethod entity);

    List<PaymentMethodDTO> toDtoList(List<PaymentMethod> entities);
}
