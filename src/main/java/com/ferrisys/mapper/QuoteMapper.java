package com.ferrisys.mapper;

import com.ferrisys.common.dto.QuoteDTO;
import com.ferrisys.common.entity.business.Client;
import com.ferrisys.common.entity.business.Quote;
import com.ferrisys.mapper.support.IdMappingSupport;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {QuoteDetailMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface QuoteMapper extends IdMappingSupport {

    @Mapping(target = "id", expression = "java(toUuid(dto.id()))")
    @Mapping(target = "client", expression = "java(mapClient(dto.clientId()))")
    Quote toEntity(QuoteDTO dto);

    @Mapping(target = "id", expression = "java(fromUuid(entity.getId()))")
    @Mapping(target = "clientId", expression = "java(fromClient(entity.getClient()))")
    QuoteDTO toDto(Quote entity);

    List<QuoteDTO> toDtoList(List<Quote> entities);

    @AfterMapping
    default void linkDetails(@MappingTarget Quote quote) {
        if (quote.getDetails() != null) {
            quote.getDetails().forEach(detail -> detail.setQuote(quote));
        }
    }

    default Client mapClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        Client client = new Client();
        client.setId(toUuid(clientId));
        return client;
    }

    default String fromClient(Client client) {
        return client == null ? null : fromUuid(client.getId());
    }
}
