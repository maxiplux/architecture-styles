package app.quantun.architecture.persistence.entity;

import app.quantun.architecture.entity.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for Order persistence.
 * Part of the persistence layer - contains all persistence-specific annotations.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    // Shipping address flattened for simplicity
    @Column(nullable = false, length = 200)
    private String shippingStreet;
    @Column(nullable = false, length = 100)
    private String shippingCity;
    @Column(length = 100)
    private String shippingState;
    @Column(nullable = false, length = 20)
    private String shippingZipCode;
    @Column(nullable = false, length = 100)
    private String shippingCountry;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = OrderStatus.PENDING;
    }
}
