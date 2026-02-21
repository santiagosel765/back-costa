package com.ferrisys.mapper;

import com.ferrisys.common.dto.QuoteDetailDTO;
import com.ferrisys.common.entity.business.QuoteDetail;
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
public interface QuoteDetailMapper extends IdMappingSupport {

    @Mapping(target = "product", expression = "java(mapProduct(dto.productId()))")
    @Mapping(target = "quote", ignore = true)
    QuoteDetail toEntity(QuoteDetailDTO dto);

    @Mapping(target = "productId", expression = "java(fromProduct(entity.getProduct()))")
    QuoteDetailDTO toDto(QuoteDetail entity);

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
