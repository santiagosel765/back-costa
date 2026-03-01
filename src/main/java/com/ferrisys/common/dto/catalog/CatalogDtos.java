package com.ferrisys.common.dto.catalog;

import com.ferrisys.common.enums.catalog.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CatalogDtos {
    public record CategoryDto(UUID id, String code, String name, UUID parentId, Integer sortOrder, Boolean active) {}
    public record CategoryTreeDto(UUID id, String name, List<CategoryTreeDto> children) {}
    public record BrandDto(UUID id, String code, String name, Boolean active) {}
    public record UomGroupDto(UUID id, String name, Boolean active) {}
    public record UomDto(UUID id, UUID groupId, String name, String symbol, Boolean isBase, Boolean active) {}
    public record UomConversionDto(UUID id, UUID groupId, UUID fromUomId, UUID toUomId, BigDecimal factor, Boolean active) {}
    public record AttributeDto(UUID id, String code, String name, AttributeType type, Boolean required, Boolean searchable, Boolean visible, ApplyToType applyTo, Boolean active) {}
    public record AttributeOptionDto(UUID id, String value, Integer sortOrder, Boolean active) {}
    public record TaxProfileDto(UUID id, String name, String description, Boolean active) {}
    public record TaxProfileTaxDto(UUID taxId, Integer sortOrder, Boolean inclusive) {}
    public record ProductDto(UUID id, ProductType type, ProductStatus status, String sku, String name, String description, UUID categoryId, UUID brandId, UUID uomId, UUID taxProfileId, BigDecimal basePrice, Boolean trackStock, Boolean trackLot, Boolean trackSerial, Boolean active) {}
    public record ProductAttributeValueDto(UUID attributeId, String valueText, BigDecimal valueNumber, Boolean valueBool, LocalDate valueDate, UUID optionId, String valueJson) {}
}
