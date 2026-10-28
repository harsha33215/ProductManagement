package com.harsha.productmanagement.controller;

import com.harsha.productmanagement.dto.ProductListResponse;
import com.harsha.productmanagement.dto.ProductPageResponse;
import com.harsha.productmanagement.dto.ProductRequest;
import com.harsha.productmanagement.dto.ProductResponse;
import com.harsha.productmanagement.entity.Product;
import com.harsha.productmanagement.service.ProductService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping({"/api/products", "/api/products/"})
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    // add bulk products
    @PostMapping("bulk")
    public ResponseEntity<List<Product>> addBulkProducts(@Valid @RequestBody List<@Valid  Product> products) {
        List<Product> savedProducts = productService.addBulkProducts(products);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedProducts);
    }

    //create product
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid  @RequestBody ProductRequest request) {
        ProductResponse savedProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedProduct);
    }

    //get product by id
    @GetMapping("{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable  Long id) {
        System.out.println("🔥 Logic App called Spring Boot! Product ID = " + id);
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);

    }

    //get products pagination
    @GetMapping
    public ResponseEntity<ProductPageResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            HttpServletRequest request) {
        String[] sortValues = request.getParameterValues("sort");
        List<String> sort = sortValues == null ? null : List.of(sortValues);
        ProductPageResponse response = productService.getProducts(
                page, size, category, brand, minPrice, maxPrice, sort);
        return ResponseEntity.ok(response);
    }

    // update product
    @PutMapping("{id}")
    public ResponseEntity<ProductResponse> updateProduct(@Valid @RequestBody ProductRequest request,@PathVariable  Long id) {
        ProductResponse updatedProduct = productService.updateProduct(request, id);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
       return  ResponseEntity.noContent().build();
    }
}
