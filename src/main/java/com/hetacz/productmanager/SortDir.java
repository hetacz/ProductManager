package com.hetacz.productmanager;

import org.jetbrains.annotations.Contract;
import org.springframework.data.domain.Sort;

public enum SortDir {
    ASC, DESC;
    @Contract(pure = true)
    public Sort.Direction toDirection() {
        return this == ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
