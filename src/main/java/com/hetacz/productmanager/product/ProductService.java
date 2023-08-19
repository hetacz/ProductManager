package com.hetacz.productmanager.product;

import com.hetacz.productmanager.category.Category;
import com.hetacz.productmanager.category.CategoryRepository;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductService {

    private static final String OTHER = "Other";
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
//    @Autowired
//    private SimpMessagingTemplate simpleMessagingTemplate;

    @Contract(pure = true)
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findAll(Sort sort) {
        return productRepository.findAll(sort);
    }

    public List<Product> findBySpecification(Specification<Product> specification, Sort sort) {
        return productRepository.findAll(specification, sort);
    }

    public List<Product> findBySpecification(String name, String description, Long min, Long max,
            LocalDateTime createdBefore, LocalDateTime createdAfter, Collection<String> categoryNames, Sort sort) {
        Specification<Product> specification = Stream.of(
                        nullCheck(Specification.anyOf(categoryNames.stream()
                                .map(ProductSpecification::hasCategoryName)
                                .collect(Collectors.toSet())), categoryNames),
                        nullCheck(ProductSpecification.hasNameLike(name), name),
                        nullCheck(ProductSpecification.hasDescriptionLike(description), description),
                        nullCheck(ProductSpecification.hasPriceLessOrEqualThan(max), max),
                        nullCheck(ProductSpecification.hasPriceGreaterOrEqualThan(min), min),
                        nullCheck(ProductSpecification.wasCreatedBefore(createdBefore), createdBefore),
                        nullCheck(ProductSpecification.wasCreatedAfter(createdAfter), createdAfter)
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);
        return specification != null ? findBySpecification(specification, sort) : findAll(sort);
    }

    @Contract(value = "_, null -> null", pure = true)
    private @Nullable Specification<Product> nullCheck(Specification<Product> specification, Collection<?> collection) {
        return collection != null && !collection.isEmpty() ? specification : null;
    }

    @SafeVarargs
    private <T> @Nullable Specification<Product> nullCheck(Specification<Product> specification, T... args) {
        return (Stream.of(args).noneMatch(Objects::isNull)) ? specification : null;
    }

    public List<Product> findBySpecification(String name, Sort sort) {
        Specification<Product> specification = ProductSpecification.hasNameLike(name);
        return productRepository.findAll(specification, sort);
    }

    @Transactional
    public Product addProduct(Product product) {
        addOtherCategoryIfNotExists(product);
        return productRepository.saveAndFlush(product);
    }

    @Transactional
    public List<Product> addProducts(@NotNull List<Product> products) {
        products.forEach(this::addOtherCategoryIfNotExists);
        return productRepository.saveAllAndFlush(products);
    }

    @Transactional
    public Product updateProduct(@NotNull Product product) {
        return productRepository.findById(product.getId()).map(productToUpdate -> {
            addOtherCategoryIfNotExists(productToUpdate);
            addProductToCategories(productToUpdate);
            return productRepository.saveAndFlush(productToUpdate);
        }).orElseThrow(() -> new IllegalArgumentException("Product with id: %d not found.".formatted(product.getId())));
    }

    @Transactional
    public Product updateProduct(Long id, String name, String description, Long price, Set<Category> categories) {
        return productRepository.findById(id).map(product -> {
            if (name != null) {
                product.setName(name);
            }
            if (description != null) {
                product.setDescription(description);
            }
            if (price != null) {
                product.setPrice(price);
            }
            if (categories != null) {
                removeOtherCategoryIfPresent(product);
                product.addCategories(categories);
            }
            addOtherCategoryIfNotExists(product);
            addProductToCategories(product);
            return productRepository.saveAndFlush(product);
        }).orElseThrow(() -> new IllegalArgumentException("Product with id: %d not found.".formatted(id)));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product productToDelete = productRepository.findById(id).orElseThrow();
        removeProductFromCategories(productToDelete);
        productRepository.deleteById(id);
    }

    public void addProductToCategories(@NotNull Product product) {
        product.getCategories().forEach(category -> {
            category.addProduct(product);
            categoryRepository.saveAndFlush(category);
        });
    }

    private void removeProductFromCategories(@NotNull Product product) {
        product.getCategories().forEach(category -> {
            category.removeProduct(product);
            categoryRepository.saveAndFlush(category);
        });
    }

    public void addOtherCategoryIfNotExists(@NotNull Product product) {
        if (!product.hasAnyCategory()) {
            addOtherCategory(!categoryRepository.existsByName(OTHER)
                    ? categoryRepository.save(new Category(OTHER))
                    : categoryRepository.findByName(OTHER).orElseThrow(), product);
        }
    }

    private void removeOtherCategoryIfPresent(@NotNull Product product) {
        if (product.getCategories().size() == 1 && product.getCategories().first().getName().equals(OTHER)) {
            product.getCategories().clear();
            Category other = categoryRepository.findByName(OTHER).orElseThrow();
            other.getProducts().remove(product);
            productRepository.save(product);
            categoryRepository.save(other);
            productRepository.flush();
            categoryRepository.flush();
        }
    }

    public void addOtherCategory(Category other, @NotNull Product product) {
        product.addCategory(other);
        other.addProduct(product);
        productRepository.save(product);
        categoryRepository.save(other);
        productRepository.flush();
        categoryRepository.flush();
    }
}
