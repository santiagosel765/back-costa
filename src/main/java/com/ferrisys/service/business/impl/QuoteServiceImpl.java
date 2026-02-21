package com.ferrisys.service.business.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.QuoteDTO;
import com.ferrisys.common.entity.business.Quote;
import com.ferrisys.common.entity.inventory.Product;
import com.ferrisys.mapper.QuoteMapper;
import com.ferrisys.repository.ClientRepository;
import com.ferrisys.repository.ProductRepository;
import com.ferrisys.repository.QuoteDetailRepository;
import com.ferrisys.repository.QuoteRepository;
import com.ferrisys.service.business.QuoteService;
import java.util.UUID;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final QuoteDetailRepository detailRepository;
    private final QuoteMapper quoteMapper;

    @Override
    @Transactional
    public void saveOrUpdate(QuoteDTO dto) {
        Quote quote = quoteMapper.toEntity(dto);

        if (quote.getClient() != null && quote.getClient().getId() != null) {
            UUID clientId = quote.getClient().getId();
            quote.setClient(clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado")));
        }

        quote = quoteRepository.save(quote);

        detailRepository.deleteByQuote(quote);
        if (quote.getDetails() != null) {
            quote.getDetails().forEach(detail -> {
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
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));
        quote.setStatus(0);
        quoteRepository.save(quote);
    }

    @Override
    public PageResponse<QuoteDTO> list(int page, int size) {
        Page<QuoteDTO> pageDto = quoteRepository.findAll(PageRequest.of(page, size))
                .map(quoteMapper::toDto);
        return PageResponse.from(pageDto);
    }
}
