package com.order.orderapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotBlank(message = "Customer email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Customer email cannot exceed 100 characters")
    private String customerEmail;

    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> orderItems;

    @Size(max = 100, message = "Coupon code cannot exceed 100 characters")
    private String couponCode;

    private double price;
    @NotNull
    private String cardNumber;
}