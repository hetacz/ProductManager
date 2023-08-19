package com.hetacz.productmanager.product;

import java.io.Serializable;
import java.util.List;

public record ProductDto(String name, String description, Long price, List<String> categories) implements Serializable {

}
