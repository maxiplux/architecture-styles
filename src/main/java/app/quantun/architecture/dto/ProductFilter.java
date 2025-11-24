package app.quantun.architecture.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilter {
    private Long categoryId;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStock;
    private Boolean active = true;
}
