package com.harsha.productmanagement.service;


import com.harsha.productmanagement.dto.ProductPageResponse;
import com.harsha.productmanagement.entity.Product;
import com.harsha.productmanagement.exception.ProductNotFoundException;
import com.harsha.productmanagement.mapper.ProductMapper;
import com.harsha.productmanagement.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.harsha.productmanagement.dto.ProductRequest;
import com.harsha.productmanagement.dto.ProductResponse;
import com.harsha.productmanagement.exception.InvalidRequestException;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "category", "price", "quantity");
    private static final Map<String, Sort.Direction> SORT_DIRECTIONS = Map.of(
            "asc", Sort.Direction.ASC,
            "desc", Sort.Direction.DESC
    );

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,ProductMapper productMapper){
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    //pagination
    public ProductPageResponse getProducts(
            int page,
            int size,
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<String> sortParameters) {
        if (page < 0 || size <= 0) {
            throw new InvalidRequestException("Page must be non-negative and size must be greater than zero");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidRequestException("minPrice must be less than or equal to maxPrice");
        }

        Pageable pageable = PageRequest.of(page, size, buildSort(sortParameters));
        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("category")),
                        category.toLowerCase(Locale.ROOT)));
            }
            if (brand != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("brand")),
                        brand.toLowerCase(Locale.ROOT)));
            }
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        Page<Product> productPage = productRepository.findAll(specification, pageable);
        List<ProductResponse> products = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .toList();
        return new ProductPageResponse(
                products,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    private Sort buildSort(List<String> sortParameters) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sortParameters != null) {
            for (String sortParameter : sortParameters) {
                String[] parts = sortParameter.split(",", -1);
                if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                    throw new InvalidRequestException("Each sort parameter must use field,direction format");
                }

                String field = parts[0];
                if (!SORTABLE_FIELDS.contains(field)) {
                    throw new InvalidRequestException("Unsupported sort field: " + field);
                }

                Sort.Direction direction = SORT_DIRECTIONS.get(parts[1].toLowerCase(Locale.ROOT));
                if (direction == null) {
                    throw new InvalidRequestException("Invalid sort direction: " + parts[1]);
                }
                orders.add(new Sort.Order(direction, field));
            }
        }
        orders.add(Sort.Order.asc("id"));
        return Sort.by(orders);
    }
    //create a product
  public  ProductResponse createProduct(ProductRequest request){
        Product product =productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);

    }
    // get product by id
   public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with " + id));
        return productMapper.toResponse(product);
    }


    // update product
   public ProductResponse updateProduct(ProductRequest request, Long id){
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with " + id));

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setQuantity(request.getQuantity());
        existingProduct.setActive(request.getActive());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setBrand(request.getBrand());
        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toResponse(updatedProduct);
    }
    // create multiple products
    public List<Product> addBulkProducts(List<Product> products){
        return productRepository.saveAll(products);
    }
    // delete a product
    public void  deleteProductById(Long id){
        productRepository.findById(id)
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with " + id));
        productRepository.deleteById(id);
    }
}
