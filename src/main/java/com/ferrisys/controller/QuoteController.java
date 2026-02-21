package com.ferrisys.controller;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.QuoteDTO;
import com.ferrisys.config.license.RequireModule;
import com.ferrisys.service.business.QuoteService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/quotes")
@RequiredArgsConstructor
@RequireModule("quotes")
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping("/save")
    public void save(@RequestBody QuoteDTO dto) {
        quoteService.saveOrUpdate(dto);
    }

    @PostMapping("/disable")
    public void disable(@RequestParam UUID id) {
        quoteService.disable(id);
    }

    @GetMapping("/list")
    public PageResponse<QuoteDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return quoteService.list(page, size);
    }
}
