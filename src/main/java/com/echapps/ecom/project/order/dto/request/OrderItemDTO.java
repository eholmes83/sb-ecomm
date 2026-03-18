package com.echapps.ecom.project.order.dto.request;


import com.echapps.ecom.project.product.dto.request.ProductRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long orderItemId;
    private ProductRequest product;
    private Integer quantity;
    private Double discount;
    private Double orderedProductPrice;
}
