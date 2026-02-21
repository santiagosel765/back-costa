package com.ferrisys.mapper;

import com.ferrisys.common.dto.PurchaseDTO;
import com.ferrisys.common.entity.business.Provider;
import com.ferrisys.common.entity.business.Purchase;
import com.ferrisys.mapper.support.IdMappingSupport;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {PurchaseDetailMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface PurchaseMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    @Mapping(target = "provider", expression = "java(mapProvider(dto.providerId()))")
    Purchase toEntity(PurchaseDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    @Mapping(target = "providerId", expression = "java(fromProvider(entity.getProvider()))")
    PurchaseDTO toDto(Purchase entity);

    List<PurchaseDTO> toDtoList(List<Purchase> entities);

    @AfterMapping
    default void linkDetails(@MappingTarget Purchase purchase) {
        if (purchase.getDetails() != null) {
            purchase.getDetails().forEach(detail -> detail.setPurchase(purchase));
        }
    }

    default Provider mapProvider(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return null;
        }
        Provider provider = new Provider();
        provider.setId(toUuid(providerId));
        return provider;
    }

    default String fromProvider(Provider provider) {
        return provider == null ? null : fromUuid(provider.getId());
    }
}
