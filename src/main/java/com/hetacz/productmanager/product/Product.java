package com.hetacz.productmanager.product;

import com.hetacz.productmanager.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product implements Serializable, Comparable<Product> {

    @ToString.Exclude
    @ManyToMany
    @JoinTable(name = "PRODUCT_CATEGORIES",
            joinColumns = @JoinColumn(name = "PRODUCT_ID"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID"))
    private final SortedSet<Category> categories = new TreeSet<>();
    @PastOrPresent
    @ToString.Include(rank = -1)
    private final LocalDateTime created = LocalDateTime.now();
    @Id
    @GeneratedValue
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @Positive
    private Long price;
    @PastOrPresent
    @ToString.Include(rank = -2)
    private LocalDateTime modified = LocalDateTime.now();

    public Product(String name, String description, Long price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Product(Long id, String name, String description, Long price) {
        this(name, description, price);
        this.id = id;
    }

    public Product(String name, String description, Long price, Category... category) {
        this(name, description, price);
        this.categories.addAll(Arrays.asList(category));
    }

    public Product(Long id, String name, String description, Long price, Category... category) {
        this(id, name, description, price);
        this.categories.addAll(Arrays.asList(category));
    }

    public Product(String name, String description, Long price, List<Category> categories) {
        this(name, description, price);
        this.categories.addAll(categories);
    }

    public Product(Long id, String name, String description, Long price, List<Category> categories) {
        this(id, name, description, price);
        this.categories.addAll(categories);
    }

    public void setName(String name) {
        this.name = name;
        updateModified();
    }

    public void setDescription(String description) {
        this.description = description;
        updateModified();
    }

    public void setPrice(Long price) {
        this.price = price;
        updateModified();
    }

    public void addCategory(Category category) {
        this.categories.add(category);
        category.addProduct(this);
        updateModified();
    }

    public void addCategories(List<Category> categories) {
        this.categories.addAll(categories);
        categories.forEach(category -> category.addProduct(this));
        updateModified();
    }

    public void deleteCategory(Category category) {
        this.categories.remove(category);
        category.getProducts().remove(this);
        updateModified();
    }

    @Deprecated(forRemoval = true)
    public void deleteCategories(@NotNull List<Category> categories) {
        categories.forEach(this.categories::remove);
        categories.forEach(category -> category.getProducts().remove(this));
        updateModified();
    }

    public void clearCategories() {
        this.categories.forEach(category -> category.getProducts().remove(this));
        this.categories.clear();
        updateModified();
    }

    public boolean hasAnyCategory() {
        return !this.categories.isEmpty();
    }

    @Override
    public int compareTo(@NotNull Product o) {
        return name.compareTo(o.name);
    }

    public String toFullString() {
        return "Product{id=%d, name=%s, description=%s, price=%d, categories=%s, created=%s, modified=%s}"
                .formatted(id, name, description, price, categories.toString(), created, modified);
    }

    private void updateModified() {
        this.modified = LocalDateTime.now();
    }
}
