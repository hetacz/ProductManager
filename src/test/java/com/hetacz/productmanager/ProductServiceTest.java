package com.hetacz.productmanager;

import com.hetacz.productmanager.category.Category;
import com.hetacz.productmanager.category.CategoryRepository;
import com.hetacz.productmanager.product.Product;
import com.hetacz.productmanager.product.ProductDto;
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
    private static final String SORCERY = "Sorcery";
    private static final String YUMMY_THINGS = "Yummy Things";
    private static final String BREAD_2 = "Bread2";
    private static final String YUMMY_BREAD = "Yummy bread";
    private static final String TEST = "test";
    private static final long ID_10004 = 10004L;
    private static final long ID_10005 = 10005L;
    private static final String OTHER = "Other";
    private static final long PRICE_100 = 100L;
    private static final long PRICE_200 = 200L;
    private static final String PRICE = "price";
    private static final long PRICE_1000 = 1000L;
    private static final long ID_10006 = 10006L;
    private static final long ID_20003 = 20003L;

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
        Product product = new Product(TEST, TEST, PRICE_100);
        Product savedProduct = productService.addProduct(product);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertTrue(foundProduct.isPresent());
        assertEquals(OTHER, foundProduct.orElseThrow().getCategories().first().getName());
    }

    @Test
    @DirtiesContext
    @Transactional
    void addProductByDtoNoCategory() {
        productService.addProduct(ProductDto.of(TEST, TEST, PRICE_100, List.of()));
        Optional<Product> foundProduct = productRepository.findByName(TEST);
        assertTrue(foundProduct.isPresent());
        assertEquals(OTHER, foundProduct.orElseThrow().getCategories().first().getName());
    }

    @Test
    @DirtiesContext
    @Transactional
    void addProductByDtoWithCategories() {
        productService.addProduct(ProductDto.of(TEST, TEST, PRICE_100, List.of(GROCERY, YUMMY_THINGS)));
        Optional<Product> foundProduct = productRepository.findByName(TEST);
        assertTrue(foundProduct.isPresent());
        assertEquals(2, foundProduct.orElseThrow().getCategories().size());
        assertTrue(categoryRepository.findByName(GROCERY)
                .orElseThrow()
                .getProducts()
                .contains(foundProduct.orElseThrow()));
        assertTrue(categoryRepository.findByName(YUMMY_THINGS)
                .orElseThrow()
                .getProducts()
                .contains(foundProduct.orElseThrow()));
    }

    @Test
    void searchForProductPartialName() {
        productService.findBySpecification("ere", Sort.unsorted())
                .forEach(product -> assertTrue(product.getName().contains("ere")));
    }

    @Test
    void searchForProductAndSort() {
        assertEquals(4,
                productService.findBySpecification("e", "a", PRICE_1000, null, null, null,
                        Set.of("A", GROCERY, "Electronics"), Sort.by(PRICE).descending()).size());
    }

    @Test
    @DirtiesContext
    @Transactional
    void updateProductByDto() {
        productService.updateProduct(ID_10005, ProductDto.of(BREAD_2, YUMMY_BREAD,
                PRICE_200, List.of(GROCERY, YUMMY_THINGS)));
        Product product = productRepository.findById(ID_10005).orElseThrow();
        assertEquals(2, product.getCategories().size());
        assertEquals(BREAD_2, product.getName());
        assertEquals(YUMMY_BREAD, product.getDescription());
        assertEquals(PRICE_200, product.getPrice());
        assertTrue(product.getModified().isBefore(LocalDateTime.now()));
        assertTrue(categoryRepository.findByName(GROCERY).isPresent());
        assertTrue(categoryRepository.findByName(GROCERY).orElseThrow().getProducts().contains(product));
        assertTrue(categoryRepository.findByName(YUMMY_THINGS).isPresent());
        assertTrue(categoryRepository.findByName(YUMMY_THINGS).orElseThrow().getProducts().contains(product));
    }

    @Test
    @Transactional
    @DirtiesContext
    void updateProduct() {
        Product product = productRepository.findById(ID_10004).orElseThrow();
        product.setName(BREAD_2);
        product.setDescription(YUMMY_BREAD);
        product.setPrice(PRICE_200);
        productService.updateProduct(product);
        Product updatedProduct = productRepository.findById(ID_10004).orElseThrow();
        assertEquals(BREAD_2, updatedProduct.getName());
        assertEquals(YUMMY_BREAD, updatedProduct.getDescription());
        assertEquals(PRICE_200, updatedProduct.getPrice());
        assertTrue(updatedProduct.getModified().isBefore(LocalDateTime.now()));
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

    @Test
    @DirtiesContext
    @Transactional
    void clearCategories() {
        productService.clearCategoriesOfProduct(ID_10006);
        Product product = productRepository.findById(ID_10006).orElseThrow();
        assertEquals(1, product.getCategories().size());
        assertEquals(OTHER, product.getCategories().first().getName());
        assertEquals(1,categoryRepository.findById(ID_20003).orElseThrow().getProducts().size());
    }

    @Test
    @DirtiesContext
    @Transactional
    void addProducts() {
        ProductDto productDto1 = ProductDto.of(TEST, TEST, PRICE_100, List.of(GROCERY, YUMMY_THINGS));
        ProductDto productDto2 = ProductDto.of(BREAD_2, YUMMY_BREAD, PRICE_200, List.of(GROCERY, SORCERY));
        productService.addProductsFromDto(List.of(productDto1, productDto2));
        assertEquals(10, productRepository.findAll().size());
        assertEquals(1, categoryRepository.findByName(SORCERY).orElseThrow().getProducts().size());
        assertEquals(1, categoryRepository.findByName(YUMMY_THINGS).orElseThrow().getProducts().size());
        assertTrue(productRepository.findByName(TEST).isPresent());
        assertTrue(productRepository.findByName(BREAD_2).isPresent());
    }
}
