package com.hetacz.productmanager.category;

import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public record CategoryDto(@Size(min = 1) String name) implements Serializable {

    @Contract("_ -> new")
    public static @NotNull CategoryDto of(String name) {
        return new CategoryDto(name);
    }
}
