package com.ferrisys.mapper;

import com.ferrisys.common.dto.PurchaseDetailDTO;
import com.ferrisys.common.entity.business.PurchaseDetail;
import com.ferrisys.common.entity.inventory.Product;
import com.ferrisys.mapper.support.IdMappingSupport;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface PurchaseDetailMapper extends IdMappingSupport {

    @Mapping(target = "product", expression = "java(mapProduct(dto.productId()))")
    @Mapping(target = "purchase", ignore = true)
    PurchaseDetail toEntity(PurchaseDetailDTO dto);

    @Mapping(target = "productId", expression = "java(fromProduct(entity.getProduct()))")
    PurchaseDetailDTO toDto(PurchaseDetail entity);

    default Product mapProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            return null;
        }
        Product product = new Product();
        product.setId(toUuid(productId));
        return product;
    }

    default String fromProduct(Product product) {
        return product == null ? null : fromUuid(product.getId());
    }
}
