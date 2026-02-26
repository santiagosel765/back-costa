package com.ferrisys.service.config;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.config.CurrencyDTO;
import com.ferrisys.common.dto.config.DocumentTypeDTO;
import com.ferrisys.common.dto.config.ParameterDTO;
import com.ferrisys.common.dto.config.PaymentMethodDTO;
import com.ferrisys.common.dto.config.TaxDTO;
import java.util.UUID;

public interface ConfigCatalogService {
    PageResponse<CurrencyDTO> listCurrencies(int page, int size, String search);
    CurrencyDTO saveCurrency(CurrencyDTO dto);
    CurrencyDTO updateCurrency(UUID id, CurrencyDTO dto);
    void deleteCurrency(UUID id);

    PageResponse<TaxDTO> listTaxes(int page, int size, String search);
    TaxDTO saveTax(TaxDTO dto);
    TaxDTO updateTax(UUID id, TaxDTO dto);
    void deleteTax(UUID id);

    PageResponse<ParameterDTO> listParameters(int page, int size, String search);
    ParameterDTO saveParameter(ParameterDTO dto);
    ParameterDTO updateParameter(UUID id, ParameterDTO dto);
    void deleteParameter(UUID id);

    PageResponse<PaymentMethodDTO> listPaymentMethods(int page, int size, String search);
    PaymentMethodDTO savePaymentMethod(PaymentMethodDTO dto);
    PaymentMethodDTO updatePaymentMethod(UUID id, PaymentMethodDTO dto);
    void deletePaymentMethod(UUID id);

    PageResponse<DocumentTypeDTO> listDocumentTypes(int page, int size, String search);
    DocumentTypeDTO saveDocumentType(DocumentTypeDTO dto);
    DocumentTypeDTO updateDocumentType(UUID id, DocumentTypeDTO dto);
    void deleteDocumentType(UUID id);
}
