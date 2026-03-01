package com.ferrisys.service.impl;

import com.ferrisys.common.dto.CategoryDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.ProductDTO;
import com.ferrisys.common.entity.inventory.Category;
import com.ferrisys.common.entity.inventory.Product;
import com.ferrisys.common.enums.catalog.ProductStatus;
import com.ferrisys.common.enums.catalog.ProductType;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.repository.CategoryRepository;
import com.ferrisys.repository.ProductRepository;
import com.ferrisys.service.InventoryService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final TenantContextHolder tenantContextHolder;

    @Override @Transactional
    public void saveOrUpdateCategory(CategoryDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Category category = dto.getId() != null ? categoryRepository.findByIdAndTenantId(dto.getId(), tenantId).orElse(new Category()) : new Category();
        category.setName(dto.getName()); category.setDescription(dto.getDescription()); category.setParentCategoryId(dto.getParentCategoryId()); category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1); category.setTenantId(tenantId); categoryRepository.save(category);
    }

    @Override @Transactional
    public void saveOrUpdateProduct(ProductDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Product product = dto.getId() != null ? productRepository.findByIdAndTenantIdAndDeletedAtIsNull(dto.getId(), tenantId).orElse(new Product()) : new Product();
        product.setTenantId(tenantId); product.setName(dto.getName()); product.setDescription(dto.getDescription()); product.setCategoryId(dto.getCategoryId()); product.setType(ProductType.PRODUCT); product.setStatus(dto.getStatus()!=null&&dto.getStatus()==0?ProductStatus.ARCHIVED:ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override @Transactional
    public void disableCategory(UUID id) { UUID tenantId = tenantContextHolder.requireTenantId(); Category c = categoryRepository.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new NotFoundException("Categoría no encontrada")); c.setStatus(0); categoryRepository.save(c); }

    @Override @Transactional
    public void disableProduct(UUID id) { UUID tenantId = tenantContextHolder.requireTenantId(); Product p = productRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId).orElseThrow(() -> new NotFoundException("Producto no encontrado")); p.setStatus(ProductStatus.ARCHIVED); productRepository.save(p); }

    @Override public PageResponse<CategoryDTO> listCategories(int page, int size) {
        UUID tenantId = tenantContextHolder.requireTenantId(); Page<Category> result = categoryRepository.findByTenantId(tenantId, PageRequest.of(page, size)); List<CategoryDTO> content = result.getContent().stream().map(c -> new CategoryDTO(c.getId(), c.getName(), c.getDescription(), c.getParentCategoryId(), c.getStatus())).toList(); return new PageResponse<>(content, result.getTotalPages(), result.getTotalElements(), result.getNumber(), result.getSize());
    }

    @Override public PageResponse<ProductDTO> listProducts(int page, int size) {
        UUID tenantId = tenantContextHolder.requireTenantId(); Page<Product> result = productRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(page, size)); List<ProductDTO> content = result.getContent().stream().map(p -> new ProductDTO(p.getId(), p.getName(), p.getDescription(), null, p.getCategoryId(), p.getStatus()==ProductStatus.ARCHIVED?0:1)).toList(); return new PageResponse<>(content, result.getTotalPages(), result.getTotalElements(), result.getNumber(), result.getSize());
    }
}
