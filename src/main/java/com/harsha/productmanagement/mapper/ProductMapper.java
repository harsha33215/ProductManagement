package com.harsha.productmanagement.mapper;

import com.harsha.productmanagement.dto.ProductRequest;
import com.harsha.productmanagement.dto.ProductResponse;
import com.harsha.productmanagement.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product toEntity(ProductRequest request){
        Product product = new Product();
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setActive(request.getActive());
        product.setName(request.getName());
        product.setQuantity(request.getQuantity());
        product.setDescription(request.getDescription());

        return product;
    }
    public ProductResponse toResponse(Product product){

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory(),
                product.getBrand(),
                product.getActive()

        );
    }
}
