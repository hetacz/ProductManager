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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductService {

    // todo adding when no categories need to be added from context.
    private static final String OTHER = "Other";
    private static final String NOT_FOUND = "Product with id: %d not found.";
    private static final String NO_PRODUCTS = "No products with ids: %s found.";
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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

    public List<Product> findBySpecification(Specification<Product> specification) {
        return productRepository.findAll(specification);
    }

    public List<Product> findBySpecification(Specification<Product> specification, Sort sort) {
        return productRepository.findAll(specification, sort);
    }

    public List<Product> findBySpecification(String name, String description, Long min, Long max,
            LocalDateTime createdBefore, LocalDateTime createdAfter, Collection<String> categoryNames, Sort sort) {
        Specification<Product> specification = Stream.of(
                        categoryNames == null || categoryNames.isEmpty()
                                ? null
                                : Specification.anyOf(categoryNames.stream()
                                        .map(ProductSpecification::hasCategoryName)
                                        .collect(Collectors.toSet())),
                        nullCheck(ProductSpecification.hasNameLike(name), name),
                        nullCheck(ProductSpecification.hasDescriptionLike(description), description),
                        nullCheck(ProductSpecification.hasPriceLessOrEqualThan(max), max),
                        nullCheck(ProductSpecification.hasPriceGreaterOrEqualThan(min), min),
                        nullCheck(ProductSpecification.wasCreatedBefore(createdBefore), createdBefore),
                        nullCheck(ProductSpecification.wasCreatedAfter(createdAfter), createdAfter))
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);
        return findProducts(specification, sort);
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
    public Product addProduct(@NotNull ProductDto productDto) {
        List<Category> categories = productDto.categories()
                .stream()
                .map(categoryName -> categoryRepository.findByName(categoryName).orElse(new Category(categoryName)))
                .toList();
        categoryRepository.saveAll(categories);
        Product product = new Product(productDto.name(), productDto.description(), productDto.price(), categories);
        productRepository.save(product);
        return organizeCategoriesOfProduct(product);
    }

    @Transactional
    public List<Product> addProductsFromDto(@NotNull List<ProductDto> productDtos) {
        List<Product> products = productDtos.stream().map(this::addProduct).toList();
        return productRepository.saveAllAndFlush(products);
    }

    @Transactional
    public List<Product> addProducts(@NotNull List<Product> products) {
        products.forEach(this::addOtherCategoryIfNotExists);
        return productRepository.saveAllAndFlush(products);
    }

    @Transactional
    public Product updateProduct(@NotNull Product product) {
        return productRepository.findById(product.getId())
                .map(productToUpdate -> {
                    productToUpdate.setName(product.getName());
                    productToUpdate.setDescription(product.getDescription());
                    productToUpdate.setPrice(product.getPrice());
                    productToUpdate.addCategories(product.getCategories().stream().toList());
                    return productToUpdate;
                })
                .map(this::organizeCategoriesOfProduct)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND.formatted(product.getId())));
    }

    @Transactional
    public Product updateProduct(Long id, ProductDto productDto) {
        return productRepository.findById(id).map(product -> {
            assignNonNullValuesToProductFromProductDto(productDto, product);
            return organizeCategoriesOfProduct(product);
        }).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND.formatted(id)));
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresentOrElse(product -> {
            removeProductFromCategories(product);
            productRepository.deleteById(id);
        }, () -> {
            throw new IllegalArgumentException(NOT_FOUND.formatted(id));
        });
    }

    @Transactional
    public void deleteProducts(List<Long> ids) {
        List<Product> products = productRepository.findAllByIdIn(ids);
        if (products.isEmpty()) {
            throw new IllegalArgumentException(NO_PRODUCTS.formatted(ids));
        }
        products.forEach(product -> {
            removeProductFromCategories(product);
            productRepository.deleteById(product.getId());
        });
    }

    public void clearCategoriesOfProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.clearCategories();
        addOtherCategoryIfNotExists(product);
        productRepository.save(product);
    }

    public void addOtherCategoryIfNotExists(@NotNull Product product) {
        if (!product.hasAnyCategory()) {
            addOtherCategory(!categoryRepository.existsByName(OTHER)
                    ? categoryRepository.save(new Category(OTHER))
                    : categoryRepository.findByName(OTHER).orElseThrow(), product);
        }
    }

    private void addProductToCategories(@NotNull Product product) {
        Collection<Category> categories = product.getCategories()
                .stream()
                .peek(category -> category.addProduct(product))
                .collect(Collectors.toSet());
        categoryRepository.saveAll(categories);
    }

    private void addOtherCategory(Category other, @NotNull Product product) {
        product.addCategory(other);
        other.addProduct(product);
        saveAndFlushProductAndCategory(product, other);
    }

    private List<Product> findProducts(Specification<Product> specification, Sort sort) {
        return specification != null ?
                (sort != null ? findBySpecification(specification, sort) : findBySpecification(specification)) :
                (sort != null ? findAll(sort) : findAll());
    }

    @NotNull
    private Product organizeCategoriesOfProduct(Product productToUpdate) {
        addOtherCategoryIfNotExists(productToUpdate);
        addProductToCategories(productToUpdate);
        return productRepository.saveAndFlush(productToUpdate);
    }

    @SafeVarargs
    private <T> @Nullable Specification<Product> nullCheck(Specification<Product> specification, T... args) {
        return (Stream.of(args).noneMatch(Objects::isNull)) ? specification : null;
    }

    private void assignNonNullValuesToProductFromProductDto(@NotNull ProductDto productDto, Product product) {
        if (productDto.name() != null) {
            product.setName(productDto.name());
        }
        if (productDto.description() != null) {
            product.setDescription(productDto.description());
        }
        if (productDto.price() != null) {
            product.setPrice(productDto.price());
        }
        if (productDto.categories() != null && !productDto.categories().isEmpty()) {
            removeOtherCategoryIfPresent(product);
            List<Category> categories = productDto.categories()
                    .stream()
                    .map(categoryName -> categoryRepository.findByName(categoryName).orElse(new Category(categoryName)))
                    .toList();
            categoryRepository.saveAllAndFlush(categories);
            product.addCategories(categories);
        }
    }

    private void removeProductFromCategories(@NotNull Product product) {
        Iterable<Category> categoriesToIterate = new HashSet<>(product.getCategories());
        categoriesToIterate.forEach(category -> {
            category.removeProduct(product);
            categoryRepository.saveAndFlush(category);
        });
    }

    private void removeOtherCategoryIfPresent(@NotNull Product product) {
        if (product.getCategories().size() == 1 && product.getCategories().first().getName().equals(OTHER)) {
            product.getCategories().clear();
            Category other = categoryRepository.findByName(OTHER).orElseThrow();
            other.getProducts().remove(product);
            saveAndFlushProductAndCategory(product, other);
        }
    }

    private void saveAndFlushProductAndCategory(@NotNull Product product, Category other) {
        productRepository.save(product);
        categoryRepository.save(other);
        productRepository.flush();
        categoryRepository.flush();
    }
}
