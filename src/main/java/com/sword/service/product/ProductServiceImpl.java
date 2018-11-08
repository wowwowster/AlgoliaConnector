package com.sword.service.product;

import com.sword.domain.Product;
import com.sword.repos.ProductRepository;
import com.sword.gsa.spis.scs.service.dto.ProductWithIdDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Objects;

public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product convertProductDTOToProduct(ProductWithIdDTO productDTO) {
        Product product = new Product();
        product.setId(Integer.valueOf(productDTO.getObjectID()));
        // TODO vérifier si logique
        product.setDateModification(LocalDateTime.now());
        product.setName(productDTO.getName());
        return product;
    }

    @Override
    public ProductWithIdDTO convertProductToProductDTO(Product product) {
        ProductWithIdDTO productDTO = new ProductWithIdDTO();
        productDTO.setObjectID(Objects.toString(product.getId()).toString());
        productDTO.setName(product.getName());
        return productDTO;
    }

    @Override
    public void addProduct(ProductWithIdDTO productDTO) {

        productRepository.save(convertProductDTOToProduct(productDTO));

    }

}