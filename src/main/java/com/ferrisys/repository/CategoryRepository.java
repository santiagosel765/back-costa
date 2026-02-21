package com.ferrisys.repository;

import com.ferrisys.common.entity.inventory.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

}