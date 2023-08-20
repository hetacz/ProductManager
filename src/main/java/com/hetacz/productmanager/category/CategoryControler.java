package com.hetacz.productmanager.category;

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
@RequestMapping("/api/categories")
public class CategoryControler {

    private static final String INVALID_BODY = "Request body is not valid";
    private static final String CATEGORIES_IDS_DELETED = "Categories with IDs: %s deleted";
    private static final String CATEGORY_ID_DELETED = "Category with ID: %d deleted";
    private static final String TOPIC_CATEGORY = "/topic/category/";
    private static final String CATEGORY_CREATED = "Created category with ID: %d, category: %s";
    private static final String CATEGORY_ADDED = "Category: {} added: {}";
    private static final String DELETED_CATEGORY = "Deleted category with ID: %d";
    private static final String CATEGORY_DELETED = "Category: {} deleted";
    private static final String UPDATED_CATEGORY = "Updated category with ID: %d, new category: %s";
    private static final String CATEGORY_UPDATED = "Category: {} updated: {}";
    private static final String CATEGORY_BY_ID = "Get category by ID: %d, category: %s";
    private static final String CATEGORY_WITH_PRODUCTS = "Category: {}, with products: {}";
    private static final String ALL_CATEGORIES = "Get all category %s";
    private static final String CATEGORIES = "Categories: {}";
    private final CategoryService service;
    private final CategoryRepository repository;
    private final SimpMessagingTemplate template;

    @Contract(pure = true)
    public CategoryControler(CategoryService service, CategoryRepository repository, SimpMessagingTemplate template) {
        this.service = service;
        this.repository = repository;
        this.template = template;
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getCategoryById(@PathVariable Long id) {
        Category category = repository.findById(id).orElseThrow();
        getCategoryWithProduct(id, category);
        URI location = getSimpleUri();
        return getOkResponseBody(location, category);
    }

    @GetMapping("/")
    public ResponseEntity<String> getAllCategories() {
        List<Category> categories = repository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        getAllCategories(categories);
        URI location = getSimpleUri();
        return getOkResponseBody(location, categories);
    }

    @PostMapping(value = "/", consumes = "application/json")
    public ResponseEntity<String> addCategory(@RequestBody @Valid CategoryDto categoryDto,
            @NotNull BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(INVALID_BODY);
        }
        Category category = service.addCategory(categoryDto);
        categoryCreated(category);
        URI location = getUriWithId(category);
        return getCreatedResponseBody(location, category);
    }

    @PostMapping(value = "/batch", consumes = "application/json")
    public ResponseEntity<String> addCategories(@RequestBody @Valid List<CategoryDto> categoryDtos,
            @NotNull BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(INVALID_BODY);
        }
        List<Category> categories = service.addCategoriesFromDto(categoryDtos);
        categories.forEach(this::categoryCreated);
        List<String> locations = categories.stream()
                .map(category -> "/api/category/%d".formatted(category.getId()))
                .toList();
        return getCreateadResponseBody(locations);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteCategory(id);
        categoryDeleted(id);
        URI location = getSimpleUri();
        return getResponseOK(id, location);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<String> deleteCategories(@RequestBody List<Long> ids) {
        if (repository.findAllByIdIn(ids).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Long> idsToDelete = repository.findAllByIdIn(ids).stream().map(Category::getId).toList();
        service.deleteCategories(idsToDelete);
        idsToDelete.forEach(this::categoryDeleted);
        return getOKResponseBody(idsToDelete);
    }

    // no validation of dto as invalid as not updated
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<String> updateCategory(@PathVariable Long id, @RequestBody CategoryDto categoryDto) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Category category = service.updateCategory(id, categoryDto);
        categoryUpdated(id, category);
        URI location = getSimpleUri();
        return getOkResponseBody(location, category);
    }

    @NotNull
    private <T> ResponseEntity<String> getOkResponseBody(URI location, @NotNull T t) {
        return ResponseEntity.ok().location(location).body(t.toString());
    }

    @NotNull
    private ResponseEntity<String> getOKResponseBody(List<Long> idsToDelete) {
        return ResponseEntity.ok().body(CATEGORIES_IDS_DELETED.formatted(idsToDelete));
    }

    @NotNull
    private ResponseEntity<String> getResponseOK(Long id, URI location) {
        return ResponseEntity.ok().location(location).body(CATEGORY_ID_DELETED.formatted(id));
    }

    @NotNull
    private ResponseEntity<String> getCreatedResponseBody(URI location, @NotNull Category category) {
        return ResponseEntity.created(location).body(category.toString());
    }

    @Contract("_ -> new")
    @NotNull
    private ResponseEntity<String> getCreateadResponseBody(@NotNull List<String> locations) {
        return new ResponseEntity<>(locations.toString(), HttpStatus.CREATED);
    }

    @NotNull
    private URI getSimpleUri() {
        return ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
    }


    private void getCategoryWithProduct(Long id, Category category) {
        template.convertAndSend(TOPIC_CATEGORY + id,
                CATEGORY_BY_ID.formatted(id, category));
        log.info(CATEGORY_WITH_PRODUCTS, category, category.getProducts().toString());
    }

    private void getAllCategories(List<Category> categories) {
        template.convertAndSend(TOPIC_CATEGORY, ALL_CATEGORIES.formatted(categories));
        log.info(CATEGORIES, categories);
        categories.forEach(category -> log.debug(CATEGORY_WITH_PRODUCTS, category.getProducts(),
                category.getProducts().toString()));
    }

    @NotNull
    private URI getUriWithId(@NotNull Category category) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("{id}").buildAndExpand(category.getId()).toUri();
    }

    private void categoryCreated(@NotNull Category category) {
        template.convertAndSend(TOPIC_CATEGORY + category.getId(),
                CATEGORY_CREATED.formatted(category.getId(), category));
        log.info(CATEGORY_ADDED, category.getId(), category);
    }

    private void categoryDeleted(Long id) {
        template.convertAndSend(TOPIC_CATEGORY + id, DELETED_CATEGORY.formatted(id));
        log.info(CATEGORY_DELETED, id);
    }

    private void categoryUpdated(Long id, Category category) {
        template.convertAndSend(TOPIC_CATEGORY + id, UPDATED_CATEGORY.formatted(id, category));
        log.info(CATEGORY_UPDATED, id, category);
    }
}
