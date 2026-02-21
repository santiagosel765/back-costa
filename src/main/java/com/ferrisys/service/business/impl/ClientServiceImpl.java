package com.ferrisys.service.business.impl;

import com.ferrisys.common.dto.ClientDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.entity.business.Client;
import com.ferrisys.repository.ClientRepository;
import com.ferrisys.service.business.ClientService;
import java.util.UUID;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public void saveOrUpdate(ClientDTO dto) {
        Client client = dto.getId() != null
                ? clientRepository.findById(dto.getId()).orElse(new Client())
                : new Client();
        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        client.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void disable(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        client.setStatus(0);
        clientRepository.save(client);
    }

    @Override
    public PageResponse<ClientDTO> list(int page, int size) {
        Page<Client> result = clientRepository.findAll(PageRequest.of(page, size));
        List<ClientDTO> content = result.getContent().stream()
                .map(c -> ClientDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .email(c.getEmail())
                        .phone(c.getPhone())
                        .address(c.getAddress())
                        .status(c.getStatus())
                        .build())
                .toList();
        return new PageResponse<>(content, result.getTotalPages(), result.getTotalElements(),
                result.getNumber(), result.getSize());
    }
}
