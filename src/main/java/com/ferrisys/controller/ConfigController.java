package com.ferrisys.controller;

import com.ferrisys.common.api.ApiResponse;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.config.CurrencyDTO;
import com.ferrisys.common.dto.config.DocumentTypeDTO;
import com.ferrisys.common.dto.config.ParameterDTO;
import com.ferrisys.common.dto.config.PaymentMethodDTO;
import com.ferrisys.common.dto.config.TaxDTO;
import com.ferrisys.config.license.RequireModule;
import com.ferrisys.service.config.ConfigCatalogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/config")
@RequiredArgsConstructor
@RequireModule("config")
public class ConfigController {

    private final ConfigCatalogService service;

    @GetMapping("/currencies")
    public ApiResponse<java.util.List<CurrencyDTO>> currencies(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "") String search) {
        PageResponse<CurrencyDTO> response = service.listCurrencies(page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }

    @PostMapping("/currencies")
    public ApiResponse<CurrencyDTO> createCurrency(@RequestBody CurrencyDTO dto) { return ApiResponse.single(service.saveCurrency(dto)); }
    @PutMapping("/currencies/{id}")
    public ApiResponse<CurrencyDTO> updateCurrency(@PathVariable UUID id, @RequestBody CurrencyDTO dto) { return ApiResponse.single(service.updateCurrency(id, dto)); }
    @DeleteMapping("/currencies/{id}")
    public ApiResponse<Void> deleteCurrency(@PathVariable UUID id) { service.deleteCurrency(id); return ApiResponse.single(null); }

    @GetMapping("/taxes")
    public ApiResponse<java.util.List<TaxDTO>> taxes(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(defaultValue = "") String search) {
        PageResponse<TaxDTO> response = service.listTaxes(page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }
    @PostMapping("/taxes")
    public ApiResponse<TaxDTO> createTax(@RequestBody TaxDTO dto) { return ApiResponse.single(service.saveTax(dto)); }
    @PutMapping("/taxes/{id}")
    public ApiResponse<TaxDTO> updateTax(@PathVariable UUID id, @RequestBody TaxDTO dto) { return ApiResponse.single(service.updateTax(id, dto)); }
    @DeleteMapping("/taxes/{id}")
    public ApiResponse<Void> deleteTax(@PathVariable UUID id) { service.deleteTax(id); return ApiResponse.single(null); }

    @GetMapping("/parameters")
    public ApiResponse<java.util.List<ParameterDTO>> parameters(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(defaultValue = "") String search) {
        PageResponse<ParameterDTO> response = service.listParameters(page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }
    @PostMapping("/parameters")
    public ApiResponse<ParameterDTO> createParameter(@RequestBody ParameterDTO dto) { return ApiResponse.single(service.saveParameter(dto)); }
    @PutMapping("/parameters/{id}")
    public ApiResponse<ParameterDTO> updateParameter(@PathVariable UUID id, @RequestBody ParameterDTO dto) { return ApiResponse.single(service.updateParameter(id, dto)); }
    @DeleteMapping("/parameters/{id}")
    public ApiResponse<Void> deleteParameter(@PathVariable UUID id) { service.deleteParameter(id); return ApiResponse.single(null); }

    @GetMapping("/payment-methods")
    public ApiResponse<java.util.List<PaymentMethodDTO>> paymentMethods(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size,
                                                                         @RequestParam(defaultValue = "") String search) {
        PageResponse<PaymentMethodDTO> response = service.listPaymentMethods(page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }
    @PostMapping("/payment-methods")
    public ApiResponse<PaymentMethodDTO> createPaymentMethod(@RequestBody PaymentMethodDTO dto) { return ApiResponse.single(service.savePaymentMethod(dto)); }
    @PutMapping("/payment-methods/{id}")
    public ApiResponse<PaymentMethodDTO> updatePaymentMethod(@PathVariable UUID id, @RequestBody PaymentMethodDTO dto) { return ApiResponse.single(service.updatePaymentMethod(id, dto)); }
    @DeleteMapping("/payment-methods/{id}")
    public ApiResponse<Void> deletePaymentMethod(@PathVariable UUID id) { service.deletePaymentMethod(id); return ApiResponse.single(null); }

    @GetMapping("/document-types")
    public ApiResponse<java.util.List<DocumentTypeDTO>> documentTypes(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size,
                                                                      @RequestParam(defaultValue = "") String search) {
        PageResponse<DocumentTypeDTO> response = service.listDocumentTypes(page, size, search);
        return ApiResponse.list(response.content(), response.totalElements(), response.page(), response.size(), response.totalPages());
    }
    @PostMapping("/document-types")
    public ApiResponse<DocumentTypeDTO> createDocumentType(@RequestBody DocumentTypeDTO dto) { return ApiResponse.single(service.saveDocumentType(dto)); }
    @PutMapping("/document-types/{id}")
    public ApiResponse<DocumentTypeDTO> updateDocumentType(@PathVariable UUID id, @RequestBody DocumentTypeDTO dto) { return ApiResponse.single(service.updateDocumentType(id, dto)); }
    @DeleteMapping("/document-types/{id}")
    public ApiResponse<Void> deleteDocumentType(@PathVariable UUID id) { service.deleteDocumentType(id); return ApiResponse.single(null); }
}
