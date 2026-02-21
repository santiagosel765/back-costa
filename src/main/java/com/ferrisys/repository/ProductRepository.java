package com.ferrisys.repository;

import com.ferrisys.common.entity.inventory.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

}
