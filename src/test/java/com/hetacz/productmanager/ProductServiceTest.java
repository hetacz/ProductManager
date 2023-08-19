package com.hetacz.productmanager;

import com.hetacz.productmanager.category.Category;
import com.hetacz.productmanager.category.CategoryRepository;
import com.hetacz.productmanager.product.Product;
import com.hetacz.productmanager.product.ProductRepository;
import com.hetacz.productmanager.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(classes = ProductmanagerApplication.class)
class ProductServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

    @Test
    void countAllProducts() {
        assertEquals(8, productRepository.findAll().size());
    }

    @Test
    @DirtiesContext
    @Transactional
    void addProduct() {
        Product product = new Product("test", "test", 100L);
        Product savedProduct = productService.addProduct(product);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertTrue(foundProduct.isPresent());
        assertEquals("Other", foundProduct.orElseThrow().getCategories().first().getName());
        log.info("Product added: {}", foundProduct.orElseThrow());
    }

    @Test
    void searchForProductPartialName() {
        productService.findBySpecification("ere", Sort.unsorted())
                .forEach(product -> assertTrue(product.getName().contains("ere")));
    }

    @Test
    void searchForProductAndSort() {
        assertEquals(4,
                productService.findBySpecification("e", "a", 1000L, null, null, null, Set.of("A", "Grocery", "Electronics"),
                        Sort.by("price").descending()).size());
    }

    @Test
    @DirtiesContext
    @Transactional
    void updateProduct() {
        Category category1 = categoryRepository.findByName("Grocery").orElse(new Category("Grocery"));
        categoryRepository.saveAndFlush(category1);
        Category category2 = categoryRepository.findByName("Yummy Things").orElse(new Category("Yummy Things"));
        categoryRepository.saveAndFlush(category2);
        productService.updateProduct(10005L, "Bread2", "Yummy bread", 200L, Set.of(category1, category2));
        Product product = productRepository.findById(10005L).orElseThrow();
        assertEquals(2, product.getCategories().size());
        assertEquals("Bread2", product.getName());
        assertEquals("Yummy bread", product.getDescription());
        assertEquals(200L, product.getPrice());
        assertTrue(product.getModified().isBefore(LocalDateTime.now()));
        assertTrue(category1.getProducts().contains(product));
        assertTrue(category2.getProducts().contains(product));
    }

    @Test
    @DirtiesContext
    @Transactional
    void deleteProduct() {
        SortedSet<Category> categories = productRepository.findById(10004L).orElseThrow().getCategories();
        productService.deleteProduct(10004L);
        Optional<Product> product = productRepository.findById(10004L);
        assertTrue(product.isEmpty());
        categories.forEach(category -> assertTrue(category.getProducts().stream().noneMatch(p -> p.getId().equals(10004L))));
    }
}
