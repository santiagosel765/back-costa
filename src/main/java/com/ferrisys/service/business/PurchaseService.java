package com.ferrisys.service.business;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.PurchaseDTO;

import java.util.UUID;

public interface PurchaseService {
    void saveOrUpdate(PurchaseDTO dto);
    void disable(UUID id);
    PageResponse<PurchaseDTO> list(int page, int size);
}
