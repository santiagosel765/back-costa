package com.ferrisys.mapper.org;

import com.ferrisys.common.dto.org.DocumentNumberingDTO;
import com.ferrisys.common.entity.org.DocumentNumbering;
import com.ferrisys.mapper.support.IdMappingSupport;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface DocumentNumberingMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    @Mapping(target = "branchId", expression = "java(fromUuid(entity.getBranch().getId()))")
    @Mapping(target = "documentTypeId", expression = "java(fromUuid(entity.getDocumentType().getId()))")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)")
    DocumentNumberingDTO toDto(DocumentNumbering entity);

    List<DocumentNumberingDTO> toDtoList(List<DocumentNumbering> entities);
}
