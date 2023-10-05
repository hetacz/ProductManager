package com.hetacz.productmanager.category;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    void deleteById(@NotNull Long id);
    boolean existsById(@NotNull Long id);
    boolean existsByName(String name);
    Optional<Category> findByName(String name);
    List<Category> findAllByIdIn(List<Long> ids);
}
