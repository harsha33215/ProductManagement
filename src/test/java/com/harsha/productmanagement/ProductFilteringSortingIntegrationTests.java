package com.harsha.productmanagement;

import com.harsha.productmanagement.entity.Product;
import com.harsha.productmanagement.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductFilteringSortingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void clearProducts() {
        productRepository.deleteAll();
    }

    @Test
    void filtersAreCombinedWithCaseInsensitiveExactTextAndInclusivePriceBounds() throws Exception {
        saveProduct("Within range", "Furniture", "Acme", "20.00", 3);
        saveProduct("At maximum price", "Furniture", "Acme", "30.00", 3);
        saveProduct("Below range", "Furniture", "Acme", "19.99", 3);
        saveProduct("Wrong brand", "Furniture", "Other", "25.00", 3);
        saveProduct("Partial category", "Furniture Sale", "Acme", "25.00", 3);
        saveProduct("Above range", "Furniture", "Acme", "30.01", 3);

        mockMvc.perform(get("/api/products")
                        .param("category", "fUrNiTuRe")
                        .param("brand", "aCME")
                        .param("minPrice", "20")
                        .param("maxPrice", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.products", hasSize(2)))
                .andExpect(jsonPath("$.products[0].name").value("Within range"))
                .andExpect(jsonPath("$.products[1].name").value("At maximum price"));
    }

    @Test
    void noFiltersPreservePaginationDefaultsAndResponseShape() throws Exception {
        for (int i = 0; i < 12; i++) {
            saveProduct("Product " + i, "Category", "Brand", "10.00", i);
        }

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.products", hasSize(10)))
                .andExpect(jsonPath("$.products[0].id").exists())
                .andExpect(jsonPath("$.products[0].name").exists());
    }

    @Test
    void repeatedSortCriteriaRetainPrecedenceAndUseIdAsFinalTieBreaker() throws Exception {
        saveProduct("Zeta", "Tools", "Brand", "100.00", 1);
        saveProduct("Book first", "Books", "Brand", "20.00", 1);
        saveProduct("Book second", "Books", "Brand", "20.00", 1);
        saveProduct("Book lower", "Books", "Brand", "10.00", 1);

        mockMvc.perform(get("/api/products")
                        .param("sort", "category,asc", "price,desc")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[*].name",
                        contains("Book first", "Book second", "Book lower", "Zeta")));
    }

    @Test
    void paginationAppliesAfterFilteringAndSorting() throws Exception {
        saveProduct("C", "Toys", "Brand", "30.00", 1);
        saveProduct("A", "Toys", "Brand", "10.00", 1);
        saveProduct("B", "Toys", "Brand", "20.00", 1);
        saveProduct("Other", "Books", "Brand", "5.00", 1);

        mockMvc.perform(get("/api/products")
                        .param("category", "toys")
                        .param("sort", "price,asc")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(1))
                .andExpect(jsonPath("$.products[*].name", contains("B")));
    }

    @Test
    void invalidSortAndPriceParametersUseBadRequestErrorResponse() throws Exception {
        assertBadRequest(get("/api/products").param("sort", "price"));
        assertBadRequest(get("/api/products").param("sort", "title,asc"));
        assertBadRequest(get("/api/products").param("sort", "price,sideways"));
        assertBadRequest(get("/api/products").param("minPrice", "20").param("maxPrice", "10"));
    }

    @Test
    void invalidNumericQueryParameterUsesBadRequestErrorResponse() throws Exception {
        assertBadRequest(get("/api/products").param("minPrice", "not-a-price"));
    }

    private void assertBadRequest(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/products"));
    }

    private void saveProduct(String name, String category, String brand, String price, int quantity) {
        Product product = new Product(
                null,
                name,
                "Test product",
                new BigDecimal(price),
                quantity,
                category,
                brand,
                true
        );
        productRepository.save(product);
    }
}
