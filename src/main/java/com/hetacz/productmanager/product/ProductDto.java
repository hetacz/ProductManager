package com.hetacz.productmanager.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public record ProductDto(@Size(min = 1) String name, @Size(min = 1) String description, @Positive Long price,
        List<String> categories) implements Serializable {

    @Contract("_, _, _, _ -> new")
    public static @NotNull ProductDto of(String name, String description, Long price, List<String> categories) {
        return new ProductDto(name, description, price, categories);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ProductDto of(String name, String description, Long price) {
        return new ProductDto(name, description, price, List.of("Other"));
    }
}
