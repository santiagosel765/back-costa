package com.ferrisys.service;

import com.ferrisys.common.dto.CategoryDTO;
import com.ferrisys.common.dto.ProductDTO;
import com.ferrisys.common.dto.PageResponse;

import java.util.UUID;

public interface InventoryService {
    void saveOrUpdateCategory(CategoryDTO dto);
    void saveOrUpdateProduct(ProductDTO dto);
    void disableCategory(UUID id);
    void disableProduct(UUID id);
    PageResponse<CategoryDTO> listCategories(int page, int size);
    PageResponse<ProductDTO> listProducts(int page, int size);
}
