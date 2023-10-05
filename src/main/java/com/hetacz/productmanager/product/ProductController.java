package com.hetacz.productmanager.product;

import com.hetacz.productmanager.SortDir;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final String BODY_INVALID = "Request body is not valid";
    private static final String PRODUCT_ID_DELETED = "Product with ID: %d deleted";
    private static final String PRODUCTS_IDS_DELETED = "Products with IDs: %s deleted";
    private static final String PRODUCT = "Product: {}";
    private static final String TOPIC_PRODUCT = "/topic/product/";
    private static final String PRODUCT_PRODUCT = "Get product by ID: %d, product: %s";
    private static final String ALL_PRODUCTS = "Get all products %s";
    private static final String PRODUCTS = "Products: %s";
    private static final String CREATE_PRODUCT = "Created product with ID: %d, product: %s";
    private static final String PRODUCT_ADDED = "Product: {} added: {}";
    private static final String PRODUCT_DELETED = "Product: {} deleted";
    private static final String DELETE_PRODUCT = "Deleted product with ID: %d";
    private static final String UPDATE_PRODUCT = "Updated product with ID: %d, new product: %s";
    private static final String PRODUCT_UPDATED = "Product: {} updated: {}";
    private final ProductService service;
    private final ProductRepository repository;
    private final SimpMessagingTemplate template;

    @Contract(pure = true)
    public ProductController(ProductService service, ProductRepository repository, SimpMessagingTemplate template) {
        this.service = service;
        this.repository = repository;
        this.template = template;
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable Long id) {
        Product product = repository.findById(id).orElseThrow();
        getProduct(id, product);
        log.info(PRODUCT, product);
        return ResponseEntity.ok().location(getSimpleUri()).body(product.toFullString());
    }

    @GetMapping("/")
    public ResponseEntity<String> getAllProducts() {
        List<Product> products = repository.findAll(createSort("id", SortDir.ASC));
        return getResponseEntity(products);
    }

    @GetMapping("/specific")
    public ResponseEntity<String> getAllProductsByCategory(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description, @RequestParam(required = false) Long min,
            @RequestParam(required = false) Long max, @RequestParam(required = false) LocalDateTime before,
            @RequestParam(required = false) LocalDateTime after,
            @RequestParam(required = false) List<String> categories, @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) SortDir sortDir
    ) {
        Sort sort = createSort(sortBy, sortDir);
        boolean allNull = Stream.of(name, description, min, max, before, after, categories)
                .allMatch(Objects::isNull);
        Supplier<List<Product>> fetcher = () -> allNull
                ? repository.findAll(sort)
                : service.findBySpecification(name, description, min, max, before, after, categories, sort);
        return getResponseEntity(fetcher.get());
    }

    @PostMapping(value = "/", consumes = "application/json")
    public ResponseEntity<String> addProduct(@RequestBody @Valid ProductDto productDto, @NotNull BindingResult result) {
        if (result.hasErrors()) {
            return reposneIsInvalid();
        }
        Product product = service.addProduct(productDto);
        productCreated(product);
        URI location = getLongUri(product);
        return ResponseEntity.created(location).body(product.toString());
    }

    @PostMapping(value = "/batch", consumes = "application/json")
    public ResponseEntity<String> addProducts(@RequestBody @Valid List<ProductDto> productDtos,
            @NotNull BindingResult result) {
        if (result.hasErrors()) {
            return reposneIsInvalid();
        }
        List<Product> products = service.addProductsFromDto(productDtos);
        products.forEach(this::productCreated);
        List<String> locations = products.stream()
                .map(product -> "/api/products/%d".formatted(product.getId()))
                .toList();
        return new ResponseEntity<>(locations.toString(), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        if (repository.findById(id).isEmpty()) {
            return responseNotFound();
        }
        service.deleteProduct(id);
        productDeleted(id);
        URI location = getSimpleUri();
        return getOKResponseWithBody(id, location);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<String> deleteProducts(@RequestBody List<Long> ids) {
        if (repository.findAllByIdIn(ids).isEmpty()) {
            return responseNotFound();
        }
        List<Long> idsToDelete = repository.findAllByIdIn(ids).stream().map(Product::getId).toList();
        service.deleteProducts(idsToDelete);
        idsToDelete.forEach(this::productDeleted);
        return getOKResponseWithBody(idsToDelete);
    }

    // no validation of dto as invalid as not updated
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<String> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        if (repository.findById(id).isEmpty()) {
            return responseNotFound();
        }
        Product product = service.updateProduct(id, productDto);
        updatedProduct(id, product);
        URI location = getSimpleUri();
        return getSimpleBodyResponse(location, product);
    }

    @PatchMapping("/{id}/clear-categories")
    public ResponseEntity<String> deleteProductsCategories(@PathVariable Long id) {
        Optional<Product> product = repository.findById(id);
        if (product.isEmpty()) {
            return responseNotFound();
        }
        service.clearCategoriesOfProduct(id);
        updatedProduct(id, product.get());
        return ResponseEntity.ok().location(getSimpleUri()).body(product.get().toFullString());
    }

    private @NotNull Sort createSort(String sortBy, SortDir sortDir) {
        String field = (sortBy != null) ? sortBy : "id";
        Sort.Direction direction = (sortDir != null) ? sortDir.toDirection() : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    @NotNull
    private ResponseEntity<String> responseNotFound() {
        return ResponseEntity.notFound().build();
    }

    @NotNull
    private URI getLongUri(@NotNull Product product) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("{id}").buildAndExpand(product.getId()).toUri();
    }

    @NotNull
    private ResponseEntity<String> reposneIsInvalid() {
        return ResponseEntity.badRequest().body(BODY_INVALID);
    }

    @NotNull
    private ResponseEntity<String> getOKResponseWithBody(Long id, URI location) {
        return ResponseEntity.ok().location(location).body(PRODUCT_ID_DELETED.formatted(id));
    }

    @NotNull
    private URI getSimpleUri() {
        return ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
    }

    @NotNull
    private ResponseEntity<String> getOKResponseWithBody(List<Long> idsToDelete) {
        return ResponseEntity.ok().body(PRODUCTS_IDS_DELETED.formatted(idsToDelete));
    }

    @NotNull
    private ResponseEntity<String> getSimpleBodyResponse(URI location, @NotNull Product product) {
        return ResponseEntity.ok().location(location).body(product.toString());
    }

    private void getProduct(Long id, Product product) {
        template.convertAndSend(TOPIC_PRODUCT + id, PRODUCT_PRODUCT.formatted(id, product));
    }

    @NotNull
    private ResponseEntity<String> getResponseEntity(List<Product> products) {
        template.convertAndSend(TOPIC_PRODUCT, ALL_PRODUCTS.formatted(products));
        log.info(PRODUCTS.formatted(products.toString()));
        URI location = getSimpleUri();
        return ResponseEntity.ok().location(location).body(products.toString());
    }

    private void productCreated(@NotNull Product product) {
        template.convertAndSend(TOPIC_PRODUCT + product.getId(), CREATE_PRODUCT.formatted(product.getId(), product));
        log.info(PRODUCT_ADDED, product.getId(), product);
    }

    private void productDeleted(Long id) {
        template.convertAndSend(TOPIC_PRODUCT + id, DELETE_PRODUCT.formatted(id));
        log.info(PRODUCT_DELETED, id);
    }

    private void updatedProduct(Long id, Product product) {
        template.convertAndSend(TOPIC_PRODUCT + id, UPDATE_PRODUCT.formatted(id, product));
        log.info(PRODUCT_UPDATED, id, product);
    }
}
