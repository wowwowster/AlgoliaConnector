package com.sword.service.config;

import com.sword.service.product.ProductService;
import com.sword.service.product.ProductServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Bean
    public ProductService getProductService() {
        return new ProductServiceImpl();
    }

}
