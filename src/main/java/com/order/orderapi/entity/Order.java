package com.order.orderapi.entity;

import com.order.orderapi.entity.suberclass.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "SEQ_CUSTOM", sequenceName = "SEQ_ORDERS", allocationSize = 1)
public class Order extends AuditableEntity {

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Size(max = 100)
    @Column(name = "customer_name")
    private String customerName;

    @NotNull
    @Column(name = "order_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderTotal;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "applied_coupon_code")
    private String appliedCouponCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "customer_transaction_id")
    private String customerTransactionId;

    @Column(name = "merchant_transaction_id")
    private String merchantTransactionId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (finalAmount == null) {
            finalAmount = orderTotal.subtract(discountAmount);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculateFinalAmount() {
        this.finalAmount = this.orderTotal.subtract(this.discountAmount);
    }
}