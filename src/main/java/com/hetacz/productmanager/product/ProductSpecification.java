package com.hetacz.productmanager.product;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@UtilityClass
public class ProductSpecification {

    public Specification<Product> hasCategoryName(String name) {
        return (root, query, builder) -> {
            query.distinct(true);
            return builder.equal(root.get("categories").get("name"), name);
        };
    }

    public Specification<Product> hasNameLike(String name) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public Specification<Product> hasDescriptionLike(String description) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public Specification<Product> hasPriceGreaterOrEqualThan(Long min) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("price"), min);
    }

    public Specification<Product> hasPriceLessOrEqualThan(Long max) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("price"), max);
    }

    public Specification<Product> wasCreatedBefore(LocalDateTime date) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("created"), date);
    }

    public Specification<Product> wasCreatedAfter(LocalDateTime date) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("created"), date);
    }

    public Specification<Product> wasModifiedBefore(LocalDateTime date) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("modified"), date);
    }

    public Specification<Product> wasModifiedAfter(LocalDateTime date) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("modified"), date);
    }
}
