package com.order.orderapi.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;

    @Size(max = 100, message = "Name on card cannot exceed 100 characters")
    private String nameOnCard;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Expiry date must be in MM/YY format")
    private String expiryDate;

    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;
}
