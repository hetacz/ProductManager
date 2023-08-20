package com.hetacz.productmanager;

import com.hetacz.productmanager.category.Category;
import com.hetacz.productmanager.category.CategoryRepository;
import com.hetacz.productmanager.category.CategoryService;
import com.hetacz.productmanager.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = ProductmanagerApplication.class)
class CategoryServiceTest {

    private static final long ID_20005 = 20005L;
    private static final long ID_20001 = 20001L;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DirtiesContext
    @Transactional
    void deleteCategory() {
        String categoryName = categoryRepository.findById(ID_20005).orElseThrow().getName();
        categoryService.deleteCategory(ID_20005);
        assertEquals(5, categoryRepository.findAll().size()); // cause OTHER added to products that would lose category
        assertEquals(0, productRepository.findAllByCategories_Name(categoryName).size());
        productRepository.findAll()
                .forEach(product -> product.getCategories()
                        .forEach(category -> log.info("Product: {}, Categories: {}", product.getName(),
                                category.getName())));
    }

    @Test
    @DirtiesContext
    @Transactional
    void deleteCategories() {
        String categoryName1 = categoryRepository.findById(ID_20001).orElseThrow().getName();
        String categoryName2 = categoryRepository.findById(ID_20005).orElseThrow().getName();
        categoryService.deleteCategories(List.of(ID_20001, ID_20005));
        assertEquals(4, categoryRepository.findAll().size()); // cause OTHER added to products that would lose category
        assertEquals(0, productRepository.findAllByCategories_Name(categoryName1).size());
        assertEquals(0, productRepository.findAllByCategories_Name(categoryName2).size());
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
        categoryService.addCategories(List.of(new Category("test1"), new Category("test2")));
        assertEquals(7, categoryRepository.findAll().size());
    }

    @Test
    @Transactional
    @DirtiesContext
    void updateCategory() {
        categoryService.updateCategory(ID_20005, "test");
        assertEquals("test", categoryRepository.findById(ID_20005).orElseThrow().getName());
    }
}
