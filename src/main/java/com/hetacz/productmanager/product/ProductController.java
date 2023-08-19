//package com.hetacz.productmanager.product;
//
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/products")
//public class ProductController {
//
//    private final ProductService productService;
//    private final ProductRepository productRepository;
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public ProductController(ProductService productService,
//            ProductRepository productRepository, SimpMessagingTemplate messagingTemplate) {
//        this.productService = productService;
//        this.productRepository = productRepository;
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    @PutMapping("/{id}")
//    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
//        return productRepository.findById(id).map(productToUpdate -> {
//            productService.addOtherCategoryIfNotExists(productToUpdate);
//            productService.addProductToCategories(productToUpdate);
//            messagingTemplate.convertAndSend("/topic/products/id/" + product.getId(), product);
//            messagingTemplate.convertAndSend("/topic/products/name/" + product.getName(), product);
//            messagingTemplate.convertAndSend("/topic/products/description/" + product.getDescription(), product);
//            messagingTemplate.convertAndSend("/topic/products/price/" + product.getPrice(), product);
//            return productRepository.saveAndFlush(productToUpdate);
//        }).orElseThrow(() -> new IllegalArgumentException("Product with id: %d not found.".formatted(product.getId())));
//    }
//}
