package com.example.simpleportal.Model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private Long productRefId;
    private String name;
    private BigDecimal price;
    private int quantity;

    public CartItem() {
    }

    public CartItem(String type, Long productRefId, String name, BigDecimal price, int quantity) {
        this.type = type;
        this.productRefId = productRefId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getProductRefId() {
        return productRefId;
    }

    public void setProductRefId(Long productRefId) {
        this.productRefId = productRefId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
