package com.hetacz.productmanager.category;

import com.hetacz.productmanager.common.SortDir;
import com.hetacz.productmanager.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryControler {

    @Autowired
    private ProductService productServicel;
//    @GetMapping("/api/products/all/sorted/{what}/{how}", produces = "application/json")
//    <element>
//    List<element> getAllProoducts(@PathVariable String sortBy, @PathVariable SortDir sortDir, BindingResult resuls) {
//        if (resuls.hasErrors()) {
//            return ResponseEntity.badRequest().body("Request body is not valid");
//        }
//
//        productServicel.
//    }
}
