package com.hetacz.productmanager;

import com.hetacz.productmanager.category.Category;
import com.hetacz.productmanager.category.CategoryRepository;
import com.hetacz.productmanager.category.CategoryService;
import com.hetacz.productmanager.product.ProductRepository;
import com.hetacz.productmanager.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = ProductmanagerApplication.class)
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DirtiesContext
    @Transactional
    void deleteCategoryById() {
        String categoryName = categoryRepository.findById(20005L).orElseThrow().getName();
        categoryService.deleteCategory(20005L);
        assertEquals(5, categoryRepository.findAll().size());
        assertEquals(0, productRepository.findAllByCategories_Name(categoryName).size());
        productRepository.findAll()
                .forEach(product -> product.getCategories()
                        .forEach(category -> log.info("Product: {}, Categories: {}", product.getName(),
                                category.getName())));
    }

    @Test
    @DirtiesContext
    @Transactional
    void addCategory() {
        assertEquals(5, categoryRepository.findAll().size());
        categoryService.addCategory(new Category("test"));
        assertEquals(6, categoryRepository.findAll().size());
    }

    @Test
    @DirtiesContext
    @Transactional
    void addCategories() {
        assertEquals(5, categoryRepository.findAll().size());
        categoryService.addCategories(Set.of(new Category("test1"), new Category("test2")));
        assertEquals(7, categoryRepository.findAll().size());
    }

    @Test
    @Transactional
    @DirtiesContext
    void updateCategory() {
        categoryService.updateCategory(20005L, "test");
        assertEquals("test", categoryRepository.findById(20005L).orElseThrow().getName());
    }
}
