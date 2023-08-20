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
        template.convertAndSend("/topic/category/" + id, "Get category by ID: %d, category: %s".formatted(id, category));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.ok().location(location).body(category.toString());
    }

    @GetMapping("/")
    public ResponseEntity<String> getAllCategories() {
        List<Category> categories = repository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        template.convertAndSend("/topic/category", "Get all category %s".formatted(categories));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.ok().location(location).body(categories.toString());
    }

    @PostMapping(value = "/", consumes = "application/json")
    public ResponseEntity<String> addCategory(@RequestBody @Valid CategoryDto categoryDto, @NotNull BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Request body is not valid");
        }
        Category category = service.addCategory(categoryDto);
        template.convertAndSend("/topic/category/" + category.getId(),
                "Created category with ID: %d, category: %s".formatted(category.getId(), category));
        log.info("Category: {} added: {}", category.getId(), category);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(category.getId())
                .toUri();
        return ResponseEntity.created(location).body(category.toString());
    }

    @PostMapping(value = "/bulk", consumes = "application/json")
    public ResponseEntity<String> addCategories(@RequestBody @Valid List<CategoryDto> categoryDtos,
            @NotNull BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Request body is not valid");
        }
        List<Category> categories = service.addCategoriesFromDto(categoryDtos);
        categories.forEach(category -> {
            template.convertAndSend("/topic/category/" + category.getId(),
                    "Created category with ID: %d, category: %s".formatted(category.getId(), category));
            log.info("Category: {} added: {}", category.getId(), category);
        });
        List<String> locations = categories.stream()
                .map(category -> "/api/category/%d".formatted(category.getId())).toList();
        return new ResponseEntity<>(locations.toString(), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteCategory(id);
        template.convertAndSend("/topic/category/" + id, "Deleted category with ID: %d".formatted(id));
        log.info("Category: {} deleted", id);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.ok().location(location).body("Category with ID: %d deleted".formatted(id));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCategories(@RequestBody List<Long> ids) {
        if (repository.findAllByIdIn(ids).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Long> idsToDelete = repository.findAllByIdIn(ids).stream().map(Category::getId).toList();
        service.deleteCategories(idsToDelete);
        idsToDelete.forEach(id -> {
            template.convertAndSend("/topic/category/" + id, "Deleted category with ID: %d".formatted(id));
            log.info("Category: {} deleted", id);
        });
        return ResponseEntity.ok().body("Categories with IDs: %s deleted".formatted(idsToDelete));
    }

    // no validation of dto as invalid as not updated
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<String> updateCategory(@PathVariable Long id, @RequestBody CategoryDto categoryDto) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Category category = service.updateCategory(id, categoryDto);
        template.convertAndSend("/topic/category/" + id,
                "Updated category with ID: %d, new category: %s".formatted(id, category));
        log.info("Category: {} updated: {}", id, category);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.ok().location(location).body(category.toString());
    }
}
