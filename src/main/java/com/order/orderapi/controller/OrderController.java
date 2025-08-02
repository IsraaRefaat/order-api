package com.order.orderapi.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.order.orderapi.service.impl.OrderServiceImpl;
import com.order.orderapi.dto.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4201")
public class OrderController {

    private final OrderServiceImpl orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestHeader(value = "Authorization") String authToken) {
        log.info("Received request to create order for customer: {}", request.getCustomerEmail());
        OrderResponse response = orderService.createOrder(request, authToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        log.info("Received request to get order with ID: {}", orderId);
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Received request to get all orders");
        List<OrderResponse> response = orderService.getAllOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerEmail}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable String customerEmail) {
        log.info("Received request to get orders for customer: {}", customerEmail);
        List<OrderResponse> response = orderService.getOrdersByCustomer(customerEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<OrderResponse> processPayment(@PathVariable Long orderId,
                                                        @RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.ok(orderService.processPayment(orderId, paymentRequest));
    }



}