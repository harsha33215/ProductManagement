package com.harsha.productmanagement.dto;

import com.harsha.productmanagement.entity.Product;

import java.util.List;

public class ProductListResponse {
    private final int count;
    private final List<ProductResponse> products;

    public ProductListResponse(int count, List<ProductResponse> products) {
        this.count = count;
        this.products = products;
    }

    public int getCount() {
        return count;
    }

    public List<ProductResponse> getProducts() {
        return products;
    }
}
