package com.ecommerce.productservice.dataloader;

import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {
    private final ProductRepository productRepository;
    @Override
    public void run(String... strings) throws Exception {
        Product product = Product.builder()
                .name("Samsung Galaxy S21")
                .description("Latest Samsung smartphone with advanced features")
                .price(BigDecimal.valueOf(799.99))
                .build();
        productRepository.save(product);
        System.out.println("Test product data loaded: " + product);
    }
}
