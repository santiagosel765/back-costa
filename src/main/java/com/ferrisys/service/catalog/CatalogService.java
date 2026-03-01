package com.ferrisys.service.catalog;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.catalog.CatalogDtos.*;
import java.util.List;
import java.util.UUID;

public interface CatalogService {
    PageResponse<CategoryDto> listCategories(int page, int size, String search, Boolean active);
    CategoryDto getCategory(UUID id);
    CategoryDto saveCategory(CategoryDto dto);
    CategoryDto updateCategory(UUID id, CategoryDto dto);
    void deleteCategory(UUID id);
    List<CategoryTreeDto> treeCategories();

    PageResponse<BrandDto> listBrands(int page, int size, String search, Boolean active);
    BrandDto getBrand(UUID id); BrandDto saveBrand(BrandDto dto); BrandDto updateBrand(UUID id, BrandDto dto); void deleteBrand(UUID id);

    PageResponse<UomGroupDto> listUomGroups(int page,int size,String search,Boolean active);
    UomGroupDto getUomGroup(UUID id); UomGroupDto saveUomGroup(UomGroupDto dto); UomGroupDto updateUomGroup(UUID id,UomGroupDto dto); void deleteUomGroup(UUID id);
    PageResponse<UomDto> listUoms(int page,int size,String search,Boolean active, UUID groupId); UomDto getUom(UUID id); UomDto saveUom(UomDto dto); UomDto updateUom(UUID id,UomDto dto); void deleteUom(UUID id);
    PageResponse<UomConversionDto> listUomConversions(int page,int size,UUID groupId); UomConversionDto getUomConversion(UUID id); UomConversionDto saveUomConversion(UomConversionDto dto); UomConversionDto updateUomConversion(UUID id,UomConversionDto dto); void deleteUomConversion(UUID id);

    PageResponse<AttributeDto> listAttributes(int page, int size, String search, Boolean active); AttributeDto getAttribute(UUID id); AttributeDto saveAttribute(AttributeDto dto); AttributeDto updateAttribute(UUID id, AttributeDto dto); void deleteAttribute(UUID id);
    List<AttributeOptionDto> getAttributeOptions(UUID attributeId); List<AttributeOptionDto> replaceAttributeOptions(UUID attributeId, List<AttributeOptionDto> options);

    PageResponse<TaxProfileDto> listTaxProfiles(int page,int size,String search,Boolean active); TaxProfileDto getTaxProfile(UUID id); TaxProfileDto saveTaxProfile(TaxProfileDto dto); TaxProfileDto updateTaxProfile(UUID id, TaxProfileDto dto); void deleteTaxProfile(UUID id);
    List<TaxProfileTaxDto> getTaxProfileTaxes(UUID id); List<TaxProfileTaxDto> replaceTaxProfileTaxes(UUID id, List<TaxProfileTaxDto> taxes);
}
