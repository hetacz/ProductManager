package com.hetacz.productmanager.category;

import com.hetacz.productmanager.product.Product;
import com.hetacz.productmanager.product.ProductRepository;
import com.hetacz.productmanager.product.ProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class CategoryService {

    private static final String NOT_FOUND = "Category with id: %d not found.";
    private static final String NO_CATEGORIES = "No categories with ids: %s found.";
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    @PersistenceContext
    private EntityManager entityManager;
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
        return categories.stream().map(this::addCategory).toList();
    }

    public List<Category> addCategoriesFromDto(@NotNull List<CategoryDto> categoryDtos) {
        return categoryDtos.stream().map(this::addCategory).toList();
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
        return updateByIdAndName(id, name);
    }

    /**
     * Updates the name of a category with the given id.
     * Does not change products.
     *
     * @param id          The id of the category to be updated.
     * @param categoryDto Contains new fields for given category (in this case name only).
     * @return The updated category with the new name, or null if no category with the given id is found.
     */
    public Category updateCategory(Long id, @NotNull CategoryDto categoryDto) {
        return updateByIdAndName(id, categoryDto.name());
    }


    /**
     * Updates a category with the given data.
     * Does not change products.
     *
     * @param category The category to be updated.
     * @return The updated category, or null if the category with the given id was not found.
     */
    public Category updateCategory(@NotNull Category category) {
        return updateByIdAndName(category.getId(), category.getName());
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.findById(id).ifPresentOrElse(category -> deleteCategory(id, category), () -> {
            throw new IllegalArgumentException(NOT_FOUND.formatted(id));
        });
    }

    @Transactional
    public void deleteCategories(List<Long> ids) {
        List<Category> categories = categoryRepository.findAllByIdIn(ids);
        if (categories.isEmpty()) {
            throw new IllegalArgumentException(NO_CATEGORIES.formatted(ids));
        }
        categories.forEach(category -> deleteCategory(category.getId(), category));
    }
    
    private Category updateByIdAndName(Long id, String name) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(name);
            return categoryRepository.saveAndFlush(category);
        }).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND.formatted(id)));
    }
    
    private void deleteCategory(Long id, @NotNull Category category) {
        Iterable<Product> productsToUpdate = new HashSet<>(category.getProducts());
        productsToUpdate.forEach(product -> {
            product.deleteCategory(category);
            productService.addOtherCategoryIfNotExists(product);
        });
        productRepository.saveAll(productsToUpdate);
        entityManager.createNativeQuery("DELETE FROM product_categories WHERE category_id = :categoryId")
                .setParameter("categoryId", id)
                .executeUpdate();
        categoryRepository.deleteById(id);
    }
}
