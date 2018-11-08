package com.sword.gsa.spis.scs.service.dto;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ProductWithIdDTO extends ProductDTO {

    private String objectID;

    public ProductWithIdDTO() {
        super();
    }

    public ProductWithIdDTO(String name, String description, String brand, ArrayList<Object> categories, String type, BigDecimal price, String price_range, String image, String url, boolean free_shipping, float popularity, float rating) {
        super(name, description, brand, categories, type, price, price_range, image, url, free_shipping, popularity, rating );
    }

    public ProductWithIdDTO(String objectID, String name, String description, String brand, ArrayList<Object> categories, String type, BigDecimal price, String price_range, String image, String url, boolean free_shipping, float popularity, float rating) {
        super(name, description, brand, categories, type, price, price_range, image, url, free_shipping, popularity, rating );
        this.objectID = objectID;
    }

    public String getObjectID() {
        return objectID;
    }

    public ProductWithIdDTO setObjectID(String objectID) {
        this.objectID = objectID;
        return this;
    }
}
