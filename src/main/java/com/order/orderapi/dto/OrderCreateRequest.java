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

    @Size(max = 100, message = "Name on card cannot exceed 100 characters")
    private String nameOnCard;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Expiry date must be in MM/YY format")
    private String expiryDate;

    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;
}