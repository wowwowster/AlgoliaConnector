package com.sword.service.product;

import com.sword.domain.Product;
import com.sword.gsa.spis.scs.service.dto.ProductWithIdDTO;

public interface ProductService {

    Product convertProductDTOToProduct(ProductWithIdDTO productDTO);

    ProductWithIdDTO convertProductToProductDTO(Product product);

    void addProduct(ProductWithIdDTO productDTO);

}
