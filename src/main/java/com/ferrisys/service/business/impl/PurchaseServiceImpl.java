package com.ferrisys.service.business.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.PurchaseDTO;
import com.ferrisys.common.entity.business.Purchase;
import com.ferrisys.common.entity.inventory.Product;
import com.ferrisys.mapper.PurchaseMapper;
import com.ferrisys.repository.ProductRepository;
import com.ferrisys.repository.ProviderRepository;
import com.ferrisys.repository.PurchaseDetailRepository;
import com.ferrisys.repository.PurchaseRepository;
import com.ferrisys.service.business.PurchaseService;
import java.util.UUID;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProviderRepository providerRepository;
    private final ProductRepository productRepository;
    private final PurchaseDetailRepository detailRepository;
    private final PurchaseMapper purchaseMapper;

    @Override
    @Transactional
    public void saveOrUpdate(PurchaseDTO dto) {
        Purchase purchase = purchaseMapper.toEntity(dto);

        if (purchase.getProvider() != null && purchase.getProvider().getId() != null) {
            UUID providerId = purchase.getProvider().getId();
            purchase.setProvider(providerRepository.findById(providerId)
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado")));
        }

        purchase = purchaseRepository.save(purchase);

        detailRepository.deleteByPurchase(purchase);
        if (purchase.getDetails() != null) {
            purchase.getDetails().forEach(detail -> {
                if (detail.getProduct() != null && detail.getProduct().getId() != null) {
                    Product product = productRepository.findById(detail.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                    detail.setProduct(product);
                }
                detailRepository.save(detail);
            });
        }
    }

    @Override
    @Transactional
    public void disable(UUID id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        purchase.setStatus(0);
        purchaseRepository.save(purchase);
    }

    @Override
    public PageResponse<PurchaseDTO> list(int page, int size) {
        Page<PurchaseDTO> pageDto = purchaseRepository.findAll(PageRequest.of(page, size))
                .map(purchaseMapper::toDto);
        return PageResponse.from(pageDto);
    }
}
