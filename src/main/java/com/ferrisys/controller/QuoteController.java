package com.ferrisys.controller;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.QuoteDTO;
import com.ferrisys.service.business.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/v1/quotes")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "modules.quotes", name = "enabled", havingValue = "true", matchIfMissing = true)
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping("/save")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('quotes')")
    public void save(@RequestBody QuoteDTO dto) {
        quoteService.saveOrUpdate(dto);
    }

    @PostMapping("/disable")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('quotes')")
    public void disable(@RequestParam UUID id) {
        quoteService.disable(id);
    }

    @GetMapping("/list")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('quotes')")
    public PageResponse<QuoteDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return quoteService.list(page, size);
    }
}
