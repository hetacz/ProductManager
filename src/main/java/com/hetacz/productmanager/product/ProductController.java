package com.hetacz.productmanager.product;

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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    private final ProductRepository repository;
    private final SimpMessagingTemplate template;

    @Contract(pure = true)
    public ProductController(ProductService service, ProductRepository repository,
            SimpMessagingTemplate template) {
        this.service = service;
        this.repository = repository;
        this.template = template;
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable Long id) {
        Product product = repository.findById(id).orElseThrow();
        template.convertAndSend("/topic/product/" + id, "Get product by ID: %d, product: %s".formatted(id, product));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.ok().location(location).body(product.toString());
    }

    @GetMapping("/")
    public ResponseEntity<String> getAllProducts() {
        List<Product> products = repository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        template.convertAndSend("/topic/products", "Get all products %s".formatted(products));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.ok().location(location).body(products.toString());
    }

    @PostMapping(value = "/", consumes = "application/json")
    public ResponseEntity<String> addProduct(@RequestBody @Valid ProductDto productDto, @NotNull BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Request body is not valid");
        }
        Product product = service.addProduct(productDto);
        template.convertAndSend("/topic/product/" + product.getId(),
                "Created product with ID: %d, product: %s".formatted(product.getId(), product));
        log.info("Product: {} added: {}", product.getId(), product);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return ResponseEntity.created(location).body(product.toString());
    }

    @PostMapping(value = "/bulk", consumes = "application/json")
    public ResponseEntity<String> addProducts(@RequestBody @Valid List<ProductDto> productDtos,
            @NotNull BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Request body is not valid");
        }
        List<Product> products = service.addProductsFromDto(productDtos);
        products.forEach(product -> {
            template.convertAndSend("/topic/product/" + product.getId(),
                    "Created product with ID: %d, product: %s".formatted(product.getId(), product));
            log.info("Product: {} added: {}", product.getId(), product);
        });
        List<String> locations = products.stream()
                .map(product -> "/api/products/%d".formatted(product.getId())).toList();
        return new ResponseEntity<>(locations.toString(), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteProduct(id);
        template.convertAndSend("/topic/product/" + id, "Deleted product with ID: %d".formatted(id));
        log.info("Product: {} deleted", id);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.ok().location(location).body("Product with ID: %d deleted".formatted(id));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteProducts(@RequestBody List<Long> ids) {
        if (repository.findAllByIdIn(ids).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Long> idsToDelete = repository.findAllByIdIn(ids).stream().map(Product::getId).toList();
        service.deleteProducts(idsToDelete);
        idsToDelete.forEach(id -> {
            template.convertAndSend("/topic/product/" + id, "Deleted product with ID: %d".formatted(id));
            log.info("Product: {} deleted", id);
        });
        return ResponseEntity.ok().body("Products with IDs: %s deleted".formatted(idsToDelete));
    }

    // no validation of dto as invalid as not updated
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<String> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = service.updateProduct(id, productDto);
        template.convertAndSend("/topic/product/" + id,
                "Updated product with ID: %d, new product: %s".formatted(id, product));
        log.info("Product: {} updated: {}", id, product);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.ok().location(location).body(product.toString());
    }
}
