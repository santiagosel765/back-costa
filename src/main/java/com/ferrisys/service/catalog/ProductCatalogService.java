package com.ferrisys.service.catalog;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.catalog.CatalogDtos.ProductAttributeValueDto;
import com.ferrisys.common.dto.catalog.CatalogDtos.ProductDto;
import com.ferrisys.common.enums.catalog.ProductStatus;
import com.ferrisys.common.enums.catalog.ProductType;
import java.util.List;
import java.util.UUID;

public interface ProductCatalogService {
    PageResponse<ProductDto> listProducts(int page, int size, String search, UUID categoryId, UUID brandId, ProductType type, ProductStatus status, Boolean active);
    ProductDto getProduct(UUID id);
    ProductDto saveProduct(ProductDto dto);
    ProductDto updateProduct(UUID id, ProductDto dto);
    void deleteProduct(UUID id);
    List<ProductAttributeValueDto> getAttributes(UUID productId);
    List<ProductAttributeValueDto> replaceAttributes(UUID productId, List<ProductAttributeValueDto> values);
}
