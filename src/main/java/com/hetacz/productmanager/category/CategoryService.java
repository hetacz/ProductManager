package com.hetacz.productmanager.category;

import com.hetacz.productmanager.product.Product;
import com.hetacz.productmanager.product.ProductRepository;
import com.hetacz.productmanager.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Contract(pure = true)
    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository,
            ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    public Category addCategory(@NotNull Category category) {
        if (!categoryRepository.existsByName(category.getName())) {
            return categoryRepository.saveAndFlush(category);
        }
        log.info("Category already exists: {}", category);
        return category;
    }

    public Category addCategory(@NotNull CategoryDto categoryDto) {
        return addCategory(new Category(categoryDto.name()));
    }

    public List<Category> addCategories(@NotNull List<Category> categories) {
        return categories.stream()
                .map(this::addCategory)
                .toList();
    }

    public List<Category> addCategoriesFromDto(@NotNull List<CategoryDto> categoryDtos) {
        return categoryDtos.stream()
                .map(this::addCategory)
                .toList();
    }

    /**
     * Updates the name of a category with the given id.
     * Does not change products.
     *
     * @param id   The id of the category to be updated.
     * @param name The new name for the category.
     * @return The updated category with the new name, or null if no category with the given id is found.
     */
    public Category updateCategory(Long id, String name) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(name);
            return categoryRepository.saveAndFlush(category);
        }).orElseThrow(() -> new IllegalArgumentException("Category with id: %d not found.".formatted(id)));
    }

    /**
     * Updates the name of a category with the given id.
     * Does not change products.
     *
     * @param id          The id of the category to be updated.
     * @param categoryDto Contains new fields for given category (in this case name only).
     * @return The updated category with the new name, or null if no category with the given id is found.
     */
    public Category updateCategory(Long id, CategoryDto categoryDto) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(categoryDto.name());
            return categoryRepository.saveAndFlush(category);
        }).orElseThrow(() -> new IllegalArgumentException("Category with id: %d not found.".formatted(id)));
    }


    /**
     * Updates a category with the given data.
     * Does not change products.
     *
     * @param category The category to be updated.
     * @return The updated category, or null if the category with the given id was not found.
     */
    public Category updateCategory(@NotNull Category category) {
        return categoryRepository.findById(category.getId())
                .map(categoryToUpdate -> {
                    categoryToUpdate.setName(category.getName());
                    return categoryRepository.saveAndFlush(categoryToUpdate);
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category with id: %d not found.".formatted(category.getId())));
    }

    public Category findCategoryByName(String name) {
        return categoryRepository.findByName(name).orElse(null);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findAll(Sort sort) {
        return categoryRepository.findAll(sort);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.findById(id).ifPresentOrElse(category -> deleteCategory(id, category), () -> {
            throw new IllegalArgumentException("Category with id: %d not found.".formatted(id));
        });
    }

    private void deleteCategory(Long id, @NotNull Category category) {
        Collection<Product> productsToUpdate = new HashSet<>();
        Iterable<Product> productsToIterate = new HashSet<>(category.getProducts());
        productsToIterate.forEach(product -> {
            product.deleteCategory(category);
            productService.addOtherCategoryIfNotExists(product);
            productsToUpdate.add(product);
        });
        productRepository.saveAll(productsToUpdate);
        categoryRepository.deleteById(id);
    }

    @Transactional
    public void deleteCategories(List<Long> ids) {
        List<Category> categories = categoryRepository.findAllByIdIn(ids);
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("No products with ids: %s found.".formatted(ids));
        }
        categories.forEach(category -> deleteCategory(category.getId(), category));
    }
}
