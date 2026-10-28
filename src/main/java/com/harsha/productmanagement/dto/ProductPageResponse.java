package com.harsha.productmanagement.dto;

import java.util.*;

public class ProductPageResponse{
    private final List<ProductResponse> products;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    public ProductPageResponse(List<ProductResponse> products, int pageNumber, int pageSize, long totalElements, int totalPages, boolean last) {
        this.products = products;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    public List<ProductResponse> getProducts() {
        return products;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isLast() {
        return last;
    }
}
