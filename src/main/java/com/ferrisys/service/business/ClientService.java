package com.ferrisys.service.business;

import com.ferrisys.common.dto.ClientDTO;
import com.ferrisys.common.dto.PageResponse;
import java.util.UUID;

public interface ClientService {
    ClientDTO saveOrUpdate(ClientDTO dto);
    void disable(UUID id);
    PageResponse<ClientDTO> list(int page, int size);
    ClientDTO getById(UUID id);
}
