package com.ferrisys.service.business;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.ProviderDTO;

import java.util.UUID;

public interface ProviderService {
    void saveOrUpdate(ProviderDTO dto);
    void disable(UUID id);
    PageResponse<ProviderDTO> list(int page, int size);
}
