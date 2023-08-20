package com.hetacz.productmanager;

import com.hetacz.productmanager.category.Category;
import com.hetacz.productmanager.product.Product;
import com.hetacz.productmanager.product.ProductDto;
import com.hetacz.productmanager.category.CategoryRepository;
import com.hetacz.productmanager.product.ProductRepository;
import com.hetacz.productmanager.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = ProductmanagerApplication.class)
class ProductServiceTest {

    private static final String GROCERY = "Grocery";
    private static final String YUMMY_THINGS = "Yummy Things";
    private static final String BREAD_2 = "Bread2";
    private static final String YUMMY_BREAD = "Yummy bread";
    private static final String TEST = "test";
    private static final long ID_10004 = 10004L;
    private static final long ID_10005 = 10005L;
    private static final String OTHER = "Other";
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
        Product product = new Product(TEST, TEST, 100L);
        Product savedProduct = productService.addProduct(product);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertTrue(foundProduct.isPresent());
        assertEquals(OTHER, foundProduct.orElseThrow().getCategories().first().getName());
    }

    @Test
    @DirtiesContext
    @Transactional
    void addProductByDtoNoCategory() {
        productService.addProduct(ProductDto.of(TEST, TEST, 100L, List.of()));
        Optional<Product> foundProduct = productRepository.findByName(TEST);
        assertTrue(foundProduct.isPresent());
        assertEquals(OTHER, foundProduct.orElseThrow().getCategories().first().getName());
    }

    @Test
    @DirtiesContext
    @Transactional
    void addProductByDtoWithCategories() {
        productService.addProduct(ProductDto.of(TEST, TEST, 100L, List.of(GROCERY, YUMMY_THINGS)));
        Optional<Product> foundProduct = productRepository.findByName(TEST);
        assertTrue(foundProduct.isPresent());
        assertEquals(2, foundProduct.orElseThrow().getCategories().size());
        assertTrue(categoryRepository.findByName(GROCERY).orElseThrow().getProducts().contains(foundProduct.orElseThrow()));
        assertTrue(categoryRepository.findByName(YUMMY_THINGS).orElseThrow().getProducts().contains(foundProduct.orElseThrow()));
    }

    @Test
    void searchForProductPartialName() {
        productService.findBySpecification("ere", Sort.unsorted())
                .forEach(product -> assertTrue(product.getName().contains("ere")));
    }

    @Test
    void searchForProductAndSort() {
        assertEquals(4,
                productService.findBySpecification("e", "a", 1000L, null, null, null, Set.of("A", GROCERY, "Electronics"),
                        Sort.by("price").descending()).size());
    }

    @Test
    @DirtiesContext
    @Transactional
    void updateProduct() {
        productService.updateProduct(ID_10005, ProductDto.of(BREAD_2, YUMMY_BREAD, 200L, List.of(GROCERY, YUMMY_THINGS)));
        Product product = productRepository.findById(ID_10005).orElseThrow();
        assertEquals(2, product.getCategories().size());
        assertEquals(BREAD_2, product.getName());
        assertEquals(YUMMY_BREAD, product.getDescription());
        assertEquals(200L, product.getPrice());
        assertTrue(product.getModified().isBefore(LocalDateTime.now()));
        assertTrue(categoryRepository.findByName(GROCERY).isPresent());
        assertTrue(categoryRepository.findByName(GROCERY).orElseThrow().getProducts().contains(product));
        assertTrue(categoryRepository.findByName(YUMMY_THINGS).isPresent());
        assertTrue(categoryRepository.findByName(YUMMY_THINGS).orElseThrow().getProducts().contains(product));
    }

    @Test
    @DirtiesContext
    @Transactional
    void deleteProduct() {
        SortedSet<Category> categories = productRepository.findById(ID_10004).orElseThrow().getCategories();
        productService.deleteProduct(ID_10004);
        Optional<Product> product = productRepository.findById(ID_10004);
        assertTrue(product.isEmpty());
        categories.forEach(category -> assertTrue(category.getProducts().stream().noneMatch(p -> p.getId().equals(
                ID_10004))));
    }
}
