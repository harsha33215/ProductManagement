package com.harsha.productmanagement;

import com.harsha.productmanagement.entity.Product;
import com.harsha.productmanagement.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres-test")
@Transactional
class ProductFilteringSortingPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void filtersAndPaginatesRepeatedlySortedProductsOnPostgresql() throws Exception {
        String category = "deployment-check-" + UUID.randomUUID();
        saveProduct("Highest", category, "30.00");
        saveProduct("Tie first", category, "20.00");
        saveProduct("Tie second", category, "20.00");
        saveProduct("Unmatched category", category + "-other", "99.00");
        productRepository.flush();

        mockMvc.perform(get("/api/products")
                        .param("category", category.toUpperCase())
                        .param("sort", "price,desc", "category,asc")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.products[*].name", contains("Highest", "Tie first")));

        mockMvc.perform(get("/api/products")
                        .param("category", category.toUpperCase())
                        .param("sort", "price,desc", "category,asc")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.products[*].name", contains("Tie second")));
    }

    private void saveProduct(String name, String category, String price) {
        productRepository.save(new Product(
                null,
                name,
                "PostgreSQL validation product",
                new BigDecimal(price),
                1,
                category,
                "Validation",
                true
        ));
    }
}
