package com.sword.gsa.spis.scs.service.dto;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ProductDTO {

    private String name;
    private String description;
    private String brand;
    ArrayList<Object> categories = new ArrayList<Object>();
    // TODO implémenter le sous-objet
    // HierarchicalCategoriesDTO HierarchicalCategoriesObject;
    private String type;
    private BigDecimal price;
    private String price_range;
    private String image;
    private String url;
    private boolean free_shipping;
    private float popularity;
    private float rating;

    public ProductDTO() {
    }

    // TODO voir si ce constructeur moche est indispensable pour Algolia
    public ProductDTO(String name, String description, String brand, ArrayList<Object> categories, String type, BigDecimal price, String price_range, String image, String url, boolean free_shipping, float popularity, float rating) {
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.categories = categories;
        this.type = type;
        this.price = price;
        this.price_range = price_range;
        this.image = image;
        this.url = url;
        this.free_shipping = free_shipping;
        this.popularity = popularity;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    // TODO implémenter le sous-objet
    /* public HierarchicalCategoriesDTO getHierarchicalCategories() {
        return HierarchicalCategoriesObject;
    } */

    public String getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getPrice_range() {
        return price_range;
    }

    public String getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }

    public boolean getFree_shipping() {
        return free_shipping;
    }

    public float getPopularity() {
        return popularity;
    }

    public float getRating() {
        return rating;
    }

    public ProductDTO setName(String name) {
        this.name = name;
        return this;
    }

    public ProductDTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public ProductDTO setBrand(String brand ) {
        this.brand = brand;
        return this;
    }

    // TODO implémenter le sous-objet
   /* public void setHierarchicalCategories( HierarchicalCategoriesDTO hierarchicalCategoriesObject ) {
        this.HierarchicalCategoriesObject = hierarchicalCategoriesObject;
    } */

    public ProductDTO setType(String type ) {
        this.type = type;
        return this;
    }

    public ProductDTO setPrice( BigDecimal price ) {
        this.price = price;
        return this;
    }

    public ProductDTO setPrice_range( String price_range ) {
        this.price_range = price_range;
        return this;
    }

    public ProductDTO setImage( String image ) {
        this.image = image;
        return this;
    }

    public ProductDTO setUrl( String url ) {
        this.url = url;
        return this;
    }

    // TODO Modifier les setters
    public void setFree_shipping( boolean free_shipping ) {
        this.free_shipping = free_shipping;
    }

    public void setPopularity( float popularity ) {
        this.popularity = popularity;
    }

    public void setRating( float rating ) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", brand='" + brand + '\'' +
                ", categories=" + categories +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", price_range='" + price_range + '\'' +
                ", image='" + image + '\'' +
                ", url='" + url + '\'' +
                ", free_shipping=" + free_shipping +
                ", popularity=" + popularity +
                ", rating=" + rating +
                '}';
    }
}

