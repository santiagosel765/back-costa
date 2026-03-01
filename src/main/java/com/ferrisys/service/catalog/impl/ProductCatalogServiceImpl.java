package com.ferrisys.service.catalog.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.catalog.CatalogDtos.ProductAttributeValueDto;
import com.ferrisys.common.dto.catalog.CatalogDtos.ProductDto;
import com.ferrisys.common.entity.inventory.Product;
import com.ferrisys.common.entity.inventory.ProductAttributeValue;
import com.ferrisys.common.enums.catalog.AttributeType;
import com.ferrisys.common.enums.catalog.ProductStatus;
import com.ferrisys.common.enums.catalog.ProductType;
import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.ConflictException;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.config.security.JWTUtil;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.repository.ProductAttributeValueRepository;
import com.ferrisys.repository.ProductRepository;
import com.ferrisys.repository.catalog.*;
import com.ferrisys.service.catalog.ProductCatalogService;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class ProductCatalogServiceImpl implements ProductCatalogService {
    private final TenantContextHolder tenant; private final JWTUtil jwt;
    private final ProductRepository productRepo; private final ProductAttributeValueRepository valueRepo;
    private final MstCategoryRepository categoryRepo; private final MstBrandRepository brandRepo; private final MstUomRepository uomRepo; private final MstTaxProfileRepository taxRepo;
    private final MstAttributeRepository attributeRepo; private final MstAttributeOptionRepository optionRepo;

    private int p(int page){return Math.max(page-1,0);} private String s(String x){return x==null?"":x.trim();}
    private PageResponse<ProductDto> build(org.springframework.data.domain.Page<ProductDto> p,int req){return new PageResponse<>(p.getContent(),p.getTotalPages(),p.getTotalElements(),req,p.getSize());}
    private ProductDto toDto(Product e){return new ProductDto(e.getId(),e.getType(),e.getStatus(),e.getSku(),e.getName(),e.getDescription(),e.getCategoryId(),e.getBrandId(),e.getUomId(),e.getTaxProfileId(),e.getBasePrice(),e.getTrackStock(),e.getTrackLot(),e.getTrackSerial(),e.getActive());}

    @Override
    public PageResponse<ProductDto> listProducts(int page, int size, String search, UUID categoryId, UUID brandId, ProductType type, ProductStatus status, Boolean active) {
        UUID t=tenant.requireTenantId();
        var p = productRepo.findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActiveAndTypeAndStatus(t,s(search),active==null?true:active,type==null?ProductType.PRODUCT:type,status==null?ProductStatus.DRAFT:status,PageRequest.of(p(page),size));
        var list = p.getContent().stream().map(this::toDto)
                .filter(x->categoryId==null||Objects.equals(x.categoryId(),categoryId))
                .filter(x->brandId==null||Objects.equals(x.brandId(),brandId))
                .toList();
        return build(new PageImpl<>(list, p.getPageable(), p.getTotalElements()),page);
    }

    @Override public ProductDto getProduct(UUID id){UUID t=tenant.requireTenantId(); return toDto(productRepo.findByIdAndTenantIdAndDeletedAtIsNull(id,t).orElseThrow(()->new NotFoundException("Producto no encontrado")));}

    @Override @Transactional
    public ProductDto saveProduct(ProductDto dto) {
        UUID t=tenant.requireTenantId(); Product e=new Product(); e.setTenantId(t); apply(e,dto,null,t); return getProduct(productRepo.save(e).getId());
    }

    @Override @Transactional
    public ProductDto updateProduct(UUID id, ProductDto dto) {
        UUID t=tenant.requireTenantId(); Product e=productRepo.findByIdAndTenantIdAndDeletedAtIsNull(id,t).orElseThrow(()->new NotFoundException("Producto no encontrado")); apply(e,dto,id,t); return getProduct(productRepo.save(e).getId());
    }

    private void apply(Product e, ProductDto dto, UUID currentId, UUID tenantId){
        if(dto.sku()!=null && !dto.sku().isBlank()) {
            boolean exists = currentId==null ? productRepo.existsByTenantIdAndSkuIgnoreCaseAndDeletedAtIsNullAndActiveTrue(tenantId,dto.sku()) : productRepo.existsByTenantIdAndSkuIgnoreCaseAndIdNotAndDeletedAtIsNullAndActiveTrue(tenantId,dto.sku(),currentId);
            if(exists) throw new ConflictException("SKU ya existe");
            e.setSku(dto.sku().trim());
        }
        e.setType(dto.type()==null?ProductType.PRODUCT:dto.type()); e.setStatus(dto.status()==null?ProductStatus.DRAFT:dto.status()); e.setName(dto.name()); e.setDescription(dto.description()); e.setBasePrice(dto.basePrice());
        e.setTrackStock(dto.trackStock()==null?false:dto.trackStock()); e.setTrackLot(dto.trackLot()==null?false:dto.trackLot()); e.setTrackSerial(dto.trackSerial()==null?false:dto.trackSerial()); e.setActive(dto.active()==null?true:dto.active());
        if(e.getBasePrice()!=null && e.getBasePrice().signum()<0) throw new BadRequestException("base_price no puede ser negativo");
        if(dto.categoryId()!=null) categoryRepo.findByIdAndTenantIdAndDeletedAtIsNull(dto.categoryId(),tenantId).orElseThrow(()->new BadRequestException("Categoría inválida"));
        if(dto.brandId()!=null) brandRepo.findByIdAndTenantIdAndDeletedAtIsNull(dto.brandId(),tenantId).orElseThrow(()->new BadRequestException("Marca inválida"));
        if(dto.uomId()!=null) uomRepo.findByIdAndTenantIdAndDeletedAtIsNull(dto.uomId(),tenantId).orElseThrow(()->new BadRequestException("UOM inválida"));
        if(dto.taxProfileId()!=null) taxRepo.findByIdAndTenantIdAndDeletedAtIsNull(dto.taxProfileId(),tenantId).orElseThrow(()->new BadRequestException("Perfil tributario inválido"));
        if(e.getStatus()==ProductStatus.ACTIVE && dto.categoryId()==null) throw new BadRequestException("Categoría requerida cuando status=ACTIVE");
        if((e.getType()==ProductType.PRODUCT || e.getType()==ProductType.KIT) && dto.uomId()==null) throw new BadRequestException("UOM requerida para PRODUCT/KIT");
        e.setCategoryId(dto.categoryId()); e.setBrandId(dto.brandId()); e.setUomId(dto.uomId()); e.setTaxProfileId(dto.taxProfileId());
    }

    @Override @Transactional public void deleteProduct(UUID id){UUID t=tenant.requireTenantId(); var e=productRepo.findByIdAndTenantIdAndDeletedAtIsNull(id,t).orElseThrow(()->new NotFoundException("Producto no encontrado")); e.setActive(false); e.setDeletedAt(OffsetDateTime.now()); e.setDeletedBy(jwt.getCurrentUser()); productRepo.save(e);} 

    @Override public List<ProductAttributeValueDto> getAttributes(UUID productId){UUID t=tenant.requireTenantId(); productRepo.findByIdAndTenantIdAndDeletedAtIsNull(productId,t).orElseThrow(()->new NotFoundException("Producto no encontrado")); return valueRepo.findByTenantIdAndProductIdAndDeletedAtIsNull(t,productId).stream().map(v->new ProductAttributeValueDto(v.getAttributeId(),v.getValueText(),v.getValueNumber(),v.getValueBool(),v.getValueDate(),v.getOptionId(),v.getValueJson())).toList();}

    @Override @Transactional
    public List<ProductAttributeValueDto> replaceAttributes(UUID productId, List<ProductAttributeValueDto> values) {
        UUID t=tenant.requireTenantId(); Product p=productRepo.findByIdAndTenantIdAndDeletedAtIsNull(productId,t).orElseThrow(()->new NotFoundException("Producto no encontrado"));
        valueRepo.findByTenantIdAndProductIdAndDeletedAtIsNull(t,productId).forEach(v->{v.setActive(false);v.setDeletedAt(OffsetDateTime.now());v.setDeletedBy(jwt.getCurrentUser()); valueRepo.save(v);});
        for(var v: values){
            var attr=attributeRepo.findByIdAndTenantIdAndDeletedAtIsNull(v.attributeId(),t).orElseThrow(()->new BadRequestException("Atributo inválido"));
            if(v.optionId()!=null){var opt=optionRepo.findByIdAndTenantIdAndDeletedAtIsNull(v.optionId(),t).orElseThrow(()->new BadRequestException("Opción inválida")); if(!Objects.equals(opt.getAttributeId(),v.attributeId())) throw new BadRequestException("option_id no corresponde al atributo");}
            ProductAttributeValue row=new ProductAttributeValue(); row.setTenantId(t); row.setProductId(productId); row.setAttributeId(v.attributeId()); row.setValueText(v.valueText()); row.setValueNumber(v.valueNumber()); row.setValueBool(v.valueBool()); row.setValueDate(v.valueDate()); row.setOptionId(v.optionId()); row.setValueJson(v.valueJson()); valueRepo.save(row);
            if(Boolean.TRUE.equals(attr.getRequired()) && isEmpty(v, attr.getType())) throw new BadRequestException("Atributo requerido sin valor");
        }
        if(p.getStatus()==ProductStatus.ACTIVE){
            var req=attributeRepo.findByTenantIdAndDeletedAtIsNullAndRequiredTrueAndActiveTrue(t);
            Set<UUID> attrs=values.stream().map(ProductAttributeValueDto::attributeId).collect(java.util.stream.Collectors.toSet());
            for(var a:req){if(!attrs.contains(a.getId())) throw new BadRequestException("Atributos requeridos incompletos para ACTIVE");}
        }
        return getAttributes(productId);
    }

    private boolean isEmpty(ProductAttributeValueDto v, AttributeType t){return switch(t){case TEXT -> v.valueText()==null||v.valueText().isBlank(); case NUMBER -> v.valueNumber()==null; case BOOLEAN -> v.valueBool()==null; case DATE -> v.valueDate()==null; case SELECT -> v.optionId()==null; case MULTISELECT -> v.valueJson()==null||v.valueJson().isBlank();};}
}
