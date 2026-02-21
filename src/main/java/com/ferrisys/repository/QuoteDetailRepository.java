package com.ferrisys.repository;

import com.ferrisys.common.entity.business.Quote;
import com.ferrisys.common.entity.business.QuoteDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuoteDetailRepository extends JpaRepository<QuoteDetail, UUID> {
    void deleteByQuote(Quote quote);
}
