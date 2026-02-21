package com.ferrisys.service.business;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.QuoteDTO;

import java.util.UUID;

public interface QuoteService {
    void saveOrUpdate(QuoteDTO dto);
    void disable(UUID id);
    PageResponse<QuoteDTO> list(int page, int size);
}
