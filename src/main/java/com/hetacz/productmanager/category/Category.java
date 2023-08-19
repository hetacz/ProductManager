package com.hetacz.productmanager.category;

import com.hetacz.productmanager.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Entity
public class Category implements Serializable, Comparable<Category> {

    @ManyToMany(mappedBy = "categories")
    @ToString.Exclude
    private final SortedSet<Product> products = new TreeSet<>();
    @Id
    @GeneratedValue
    private Long id;
    @Setter
    private String name;

    public Category(String name) {
        super();
        this.name = name;
    }

    public Category(Long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public void addProduct(Product product) {
        this.products.add(product);
        product.getCategories().add(this);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
        product.getCategories().remove(this);
    }

    @Override
    public int compareTo(@NotNull Category o) {
        return this.name.compareTo(o.name);
    }
}
