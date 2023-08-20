package com.hetacz.productmanager.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByName(String name);
    List<Product> findAllByIdIn(List<Long> ids);
    List<Product> findAllByNameContaining(String name);
    List<Product> findAllByDescriptionContaining(String description);
    List<Product> findAllByPriceLessThanEqual(Long price);
    List<Product> findAllByPriceGreaterThanEqual(Long price);
    List<Product> findAllByCreatedBefore(LocalDateTime date);
    List<Product> findAllByCreatedAfter(LocalDateTime date);
    List<Product> findAllByCategories_Name(String name);
}
