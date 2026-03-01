package com.ferrisys.controller.inv;

import com.ferrisys.common.api.ApiResponse;
import com.ferrisys.common.dto.catalog.CatalogDtos.ProductAttributeValueDto;
import com.ferrisys.common.dto.catalog.CatalogDtos.ProductDto;
import com.ferrisys.common.enums.catalog.ProductStatus;
import com.ferrisys.common.enums.catalog.ProductType;
import com.ferrisys.service.catalog.ProductCatalogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inv/products")
@RequiredArgsConstructor
public class ProductCatalogController {
    private final ProductCatalogService service;

    @GetMapping
    @PreAuthorize("hasAuthority('INV_PRODUCT_READ') or hasRole('ADMIN')")
    public ApiResponse<List<ProductDto>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(defaultValue = "") String search,
                                              @RequestParam(required = false) UUID categoryId,
                                              @RequestParam(required = false) UUID brandId,
                                              @RequestParam(required = false) ProductType type,
                                              @RequestParam(required = false) ProductStatus status,
                                              @RequestParam(defaultValue = "true") Boolean active) {
        var r = service.listProducts(page, size, search, categoryId, brandId, type, status, active);
        return ApiResponse.list(r.content(), r.totalElements(), r.page(), r.size(), r.totalPages());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INV_PRODUCT_READ') or hasRole('ADMIN')")
    public ApiResponse<ProductDto> get(@PathVariable UUID id) { return ApiResponse.single(service.getProduct(id)); }

    @PostMapping
    @PreAuthorize("hasAuthority('INV_PRODUCT_WRITE') or hasRole('ADMIN')")
    public ApiResponse<ProductDto> create(@RequestBody ProductDto dto) { return ApiResponse.single(service.saveProduct(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INV_PRODUCT_WRITE') or hasRole('ADMIN')")
    public ApiResponse<ProductDto> update(@PathVariable UUID id, @RequestBody ProductDto dto) { return ApiResponse.single(service.updateProduct(id, dto)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INV_PRODUCT_DELETE') or hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID id) { service.deleteProduct(id); return ApiResponse.single(null); }

    @GetMapping("/{id}/attributes")
    @PreAuthorize("hasAuthority('INV_PRODUCT_READ') or hasRole('ADMIN')")
    public ApiResponse<List<ProductAttributeValueDto>> getAttributes(@PathVariable UUID id) { return ApiResponse.single(service.getAttributes(id)); }

    @PutMapping("/{id}/attributes")
    @PreAuthorize("hasAuthority('INV_PRODUCT_WRITE') or hasRole('ADMIN')")
    public ApiResponse<List<ProductAttributeValueDto>> replaceAttributes(@PathVariable UUID id, @RequestBody List<ProductAttributeValueDto> values) {
        return ApiResponse.single(service.replaceAttributes(id, values));
    }
}
