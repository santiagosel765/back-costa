package com.ferrisys.controller.mst;

import com.ferrisys.common.api.ApiResponse;
import com.ferrisys.common.dto.catalog.CatalogDtos.*;
import com.ferrisys.common.enums.catalog.*;
import com.ferrisys.service.catalog.CatalogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mst")
@RequiredArgsConstructor
public class MasterDataController {
    private final CatalogService service;

    @GetMapping("/categories") @PreAuthorize("hasAuthority('MST_CATEGORY_READ') or hasRole('ADMIN')")
    public ApiResponse<List<CategoryDto>> categories(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="true") Boolean active){var r=service.listCategories(page,size,search,active); return ApiResponse.list(r.content(),r.totalElements(),r.page(),r.size(),r.totalPages());}
    @GetMapping("/categories/{id}") @PreAuthorize("hasAuthority('MST_CATEGORY_READ') or hasRole('ADMIN')") public ApiResponse<CategoryDto> category(@PathVariable UUID id){return ApiResponse.single(service.getCategory(id));}
    @PostMapping("/categories") @PreAuthorize("hasAuthority('MST_CATEGORY_WRITE') or hasRole('ADMIN')") public ApiResponse<CategoryDto> createCategory(@RequestBody CategoryDto dto){return ApiResponse.single(service.saveCategory(dto));}
    @PutMapping("/categories/{id}") @PreAuthorize("hasAuthority('MST_CATEGORY_WRITE') or hasRole('ADMIN')") public ApiResponse<CategoryDto> updateCategory(@PathVariable UUID id,@RequestBody CategoryDto dto){return ApiResponse.single(service.updateCategory(id,dto));}
    @DeleteMapping("/categories/{id}") @PreAuthorize("hasAuthority('MST_CATEGORY_DELETE') or hasRole('ADMIN')") public ApiResponse<Void> deleteCategory(@PathVariable UUID id){service.deleteCategory(id);return ApiResponse.single(null);}    
    @GetMapping("/categories/tree") @PreAuthorize("hasAuthority('MST_CATEGORY_READ') or hasRole('ADMIN')") public ApiResponse<List<CategoryTreeDto>> tree(){return ApiResponse.single(service.treeCategories());}

    @GetMapping("/brands") @PreAuthorize("hasAuthority('MST_BRAND_READ') or hasRole('ADMIN')")
    public ApiResponse<List<BrandDto>> brands(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="true") Boolean active){var r=service.listBrands(page,size,search,active); return ApiResponse.list(r.content(),r.totalElements(),r.page(),r.size(),r.totalPages());}
    @GetMapping("/brands/{id}") @PreAuthorize("hasAuthority('MST_BRAND_READ') or hasRole('ADMIN')") public ApiResponse<BrandDto> brand(@PathVariable UUID id){return ApiResponse.single(service.getBrand(id));}
    @PostMapping("/brands") @PreAuthorize("hasAuthority('MST_BRAND_WRITE') or hasRole('ADMIN')") public ApiResponse<BrandDto> createBrand(@RequestBody BrandDto dto){return ApiResponse.single(service.saveBrand(dto));}
    @PutMapping("/brands/{id}") @PreAuthorize("hasAuthority('MST_BRAND_WRITE') or hasRole('ADMIN')") public ApiResponse<BrandDto> updateBrand(@PathVariable UUID id,@RequestBody BrandDto dto){return ApiResponse.single(service.updateBrand(id,dto));}
    @DeleteMapping("/brands/{id}") @PreAuthorize("hasAuthority('MST_BRAND_DELETE') or hasRole('ADMIN')") public ApiResponse<Void> deleteBrand(@PathVariable UUID id){service.deleteBrand(id);return ApiResponse.single(null);} 

    @GetMapping("/uom-groups") @PreAuthorize("hasAuthority('MST_UOM_READ') or hasRole('ADMIN')")
    public ApiResponse<List<UomGroupDto>> uomGroups(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="true") Boolean active){var r=service.listUomGroups(page,size,search,active); return ApiResponse.list(r.content(),r.totalElements(),r.page(),r.size(),r.totalPages());}
    @PostMapping("/uom-groups") @PreAuthorize("hasAuthority('MST_UOM_WRITE') or hasRole('ADMIN')") public ApiResponse<UomGroupDto> createUomGroup(@RequestBody UomGroupDto dto){return ApiResponse.single(service.saveUomGroup(dto));}
    @PutMapping("/uom-groups/{id}") @PreAuthorize("hasAuthority('MST_UOM_WRITE') or hasRole('ADMIN')") public ApiResponse<UomGroupDto> updateUomGroup(@PathVariable UUID id,@RequestBody UomGroupDto dto){return ApiResponse.single(service.updateUomGroup(id,dto));}
    @DeleteMapping("/uom-groups/{id}") @PreAuthorize("hasAuthority('MST_UOM_DELETE') or hasRole('ADMIN')") public ApiResponse<Void> deleteUomGroup(@PathVariable UUID id){service.deleteUomGroup(id);return ApiResponse.single(null);} 

    @GetMapping("/uoms") @PreAuthorize("hasAuthority('MST_UOM_READ') or hasRole('ADMIN')")
    public ApiResponse<List<UomDto>> uoms(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="true") Boolean active,@RequestParam(required=false) UUID groupId){var r=service.listUoms(page,size,search,active,groupId); return ApiResponse.list(r.content(),r.totalElements(),r.page(),r.size(),r.totalPages());}
    @PostMapping("/uoms") @PreAuthorize("hasAuthority('MST_UOM_WRITE') or hasRole('ADMIN')") public ApiResponse<UomDto> createUom(@RequestBody UomDto dto){return ApiResponse.single(service.saveUom(dto));}
    @PutMapping("/uoms/{id}") @PreAuthorize("hasAuthority('MST_UOM_WRITE') or hasRole('ADMIN')") public ApiResponse<UomDto> updateUom(@PathVariable UUID id,@RequestBody UomDto dto){return ApiResponse.single(service.updateUom(id,dto));}
    @DeleteMapping("/uoms/{id}") @PreAuthorize("hasAuthority('MST_UOM_DELETE') or hasRole('ADMIN')") public ApiResponse<Void> deleteUom(@PathVariable UUID id){service.deleteUom(id);return ApiResponse.single(null);} 

    @GetMapping("/uom-conversions") @PreAuthorize("hasAuthority('MST_UOM_READ') or hasRole('ADMIN')")
    public ApiResponse<List<UomConversionDto>> conv(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam UUID groupId){var r=service.listUomConversions(page,size,groupId); return ApiResponse.list(r.content(),r.totalElements(),r.page(),r.size(),r.totalPages());}
    @PostMapping("/uom-conversions") @PreAuthorize("hasAuthority('MST_UOM_WRITE') or hasRole('ADMIN')") public ApiResponse<UomConversionDto> createConv(@RequestBody UomConversionDto dto){return ApiResponse.single(service.saveUomConversion(dto));}
    @PutMapping("/uom-conversions/{id}") @PreAuthorize("hasAuthority('MST_UOM_WRITE') or hasRole('ADMIN')") public ApiResponse<UomConversionDto> updateConv(@PathVariable UUID id,@RequestBody UomConversionDto dto){return ApiResponse.single(service.updateUomConversion(id,dto));}
    @DeleteMapping("/uom-conversions/{id}") @PreAuthorize("hasAuthority('MST_UOM_DELETE') or hasRole('ADMIN')") public ApiResponse<Void> deleteConv(@PathVariable UUID id){service.deleteUomConversion(id);return ApiResponse.single(null);} 

    @GetMapping("/attributes") @PreAuthorize("hasAuthority('MST_ATTRIBUTE_READ') or hasRole('ADMIN')")
    public ApiResponse<List<AttributeDto>> attributes(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="true") Boolean active){var r=service.listAttributes(page,size,search,active); return ApiResponse.list(r.content(),r.totalElements(),r.page(),r.size(),r.totalPages());}
    @GetMapping("/attributes/{id}") @PreAuthorize("hasAuthority('MST_ATTRIBUTE_READ') or hasRole('ADMIN')") public ApiResponse<AttributeDto> attribute(@PathVariable UUID id){return ApiResponse.single(service.getAttribute(id));}
    @PostMapping("/attributes") @PreAuthorize("hasAuthority('MST_ATTRIBUTE_WRITE') or hasRole('ADMIN')") public ApiResponse<AttributeDto> createAttribute(@RequestBody AttributeDto dto){return ApiResponse.single(service.saveAttribute(dto));}
    @PutMapping("/attributes/{id}") @PreAuthorize("hasAuthority('MST_ATTRIBUTE_WRITE') or hasRole('ADMIN')") public ApiResponse<AttributeDto> updateAttribute(@PathVariable UUID id,@RequestBody AttributeDto dto){return ApiResponse.single(service.updateAttribute(id,dto));}
    @DeleteMapping("/attributes/{id}") @PreAuthorize("hasAuthority('MST_ATTRIBUTE_DELETE') or hasRole('ADMIN')") public ApiResponse<Void> deleteAttribute(@PathVariable UUID id){service.deleteAttribute(id);return ApiResponse.single(null);} 
    @GetMapping("/attributes/{id}/options") @PreAuthorize("hasAuthority('MST_ATTRIBUTE_READ') or hasRole('ADMIN')") public ApiResponse<List<AttributeOptionDto>> options(@PathVariable UUID id){return ApiResponse.single(service.getAttributeOptions(id));}
    @PutMapping("/attributes/{id}/options") @PreAuthorize("hasAuthority('MST_ATTRIBUTE_WRITE') or hasRole('ADMIN')") public ApiResponse<List<AttributeOptionDto>> replaceOptions(@PathVariable UUID id,@RequestBody List<AttributeOptionDto> options){return ApiResponse.single(service.replaceAttributeOptions(id,options));}

    @GetMapping("/tax-profiles") @PreAuthorize("hasAuthority('MST_TAX_PROFILE_READ') or hasRole('ADMIN')")
    public ApiResponse<List<TaxProfileDto>> taxProfiles(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="true") Boolean active){var r=service.listTaxProfiles(page,size,search,active); return ApiResponse.list(r.content(),r.totalElements(),r.page(),r.size(),r.totalPages());}
    @GetMapping("/tax-profiles/{id}") @PreAuthorize("hasAuthority('MST_TAX_PROFILE_READ') or hasRole('ADMIN')") public ApiResponse<TaxProfileDto> taxProfile(@PathVariable UUID id){return ApiResponse.single(service.getTaxProfile(id));}
    @PostMapping("/tax-profiles") @PreAuthorize("hasAuthority('MST_TAX_PROFILE_WRITE') or hasRole('ADMIN')") public ApiResponse<TaxProfileDto> createTaxProfile(@RequestBody TaxProfileDto dto){return ApiResponse.single(service.saveTaxProfile(dto));}
    @PutMapping("/tax-profiles/{id}") @PreAuthorize("hasAuthority('MST_TAX_PROFILE_WRITE') or hasRole('ADMIN')") public ApiResponse<TaxProfileDto> updateTaxProfile(@PathVariable UUID id,@RequestBody TaxProfileDto dto){return ApiResponse.single(service.updateTaxProfile(id,dto));}
    @DeleteMapping("/tax-profiles/{id}") @PreAuthorize("hasAuthority('MST_TAX_PROFILE_DELETE') or hasRole('ADMIN')") public ApiResponse<Void> deleteTaxProfile(@PathVariable UUID id){service.deleteTaxProfile(id);return ApiResponse.single(null);} 
    @GetMapping("/tax-profiles/{id}/taxes") @PreAuthorize("hasAuthority('MST_TAX_PROFILE_READ') or hasRole('ADMIN')") public ApiResponse<List<TaxProfileTaxDto>> profileTaxes(@PathVariable UUID id){return ApiResponse.single(service.getTaxProfileTaxes(id));}
    @PutMapping("/tax-profiles/{id}/taxes") @PreAuthorize("hasAuthority('MST_TAX_PROFILE_WRITE') or hasRole('ADMIN')") public ApiResponse<List<TaxProfileTaxDto>> replaceProfileTaxes(@PathVariable UUID id,@RequestBody List<TaxProfileTaxDto> taxes){return ApiResponse.single(service.replaceTaxProfileTaxes(id,taxes));}
}
