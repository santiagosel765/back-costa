package com.ferrisys.repository;

import com.ferrisys.common.entity.business.Purchase;
import com.ferrisys.common.entity.business.PurchaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, UUID> {
    void deleteByPurchase(Purchase purchase);
}
