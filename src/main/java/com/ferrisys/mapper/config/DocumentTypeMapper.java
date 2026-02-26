package com.ferrisys.mapper.config;

import com.ferrisys.common.dto.config.DocumentTypeDTO;
import com.ferrisys.common.entity.config.DocumentType;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface DocumentTypeMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    DocumentType toEntity(DocumentTypeDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    DocumentTypeDTO toDto(DocumentType entity);

    List<DocumentTypeDTO> toDtoList(List<DocumentType> entities);
}
