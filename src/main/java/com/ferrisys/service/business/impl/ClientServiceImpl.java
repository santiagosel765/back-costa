package com.ferrisys.service.business.impl;

import com.ferrisys.common.dto.ClientDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.entity.business.Client;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.repository.ClientRepository;
import com.ferrisys.service.business.ClientService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final TenantContextHolder tenantContextHolder;

    @Override
    @Transactional
    public ClientDTO saveOrUpdate(ClientDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Client client = dto.getId() != null
                ? clientRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(dto.getId(), tenantId)
                        .orElse(new Client())
                : new Client();
        client.setTenantId(tenantId);
        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        client.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        client.setActive(Boolean.TRUE);
        client.setDeletedAt(null);

        return toDto(clientRepository.save(client));
    }

    @Override
    @Transactional
    public void disable(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Client client = clientRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        client.setStatus(0);
        client.setActive(Boolean.FALSE);
        client.setDeletedAt(OffsetDateTime.now());
        clientRepository.save(client);
    }

    @Override
    public PageResponse<ClientDTO> list(int page, int size) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Page<Client> result = clientRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId, PageRequest.of(page, size));
        List<ClientDTO> content = result.getContent().stream().map(this::toDto).toList();
        return new PageResponse<>(content, result.getTotalPages(), result.getTotalElements(), result.getNumber(), result.getSize());
    }

    @Override
    public ClientDTO getById(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Client client = clientRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        return toDto(client);
    }

    private ClientDTO toDto(Client c) {
        return ClientDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .status(c.getStatus())
                .build();
    }
}
