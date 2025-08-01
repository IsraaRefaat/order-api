package com.order.orderapi.service.impl;


import com.order.orderapi.client.CategoryServiceClient;
import com.order.orderapi.client.CouponServiceClient;
import com.order.orderapi.client.UserServiceClient;
import com.order.orderapi.dto.*;
import com.order.orderapi.entity.Order;
import com.order.orderapi.entity.OrderItem;
import com.order.orderapi.entity.OrderStatus;
import com.order.orderapi.rabbit.OrderProducer;
import com.order.orderapi.rabbit.StockMessageSender;
import com.order.orderapi.repository.OrderRepository;
import com.order.orderapi.service.BankServiceImp;
import com.order.orderapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CouponServiceClient couponServiceClient;
    private final CategoryServiceClient categoryServiceClient;
    private final UserServiceClient userServiceClient;
    private final OrderProducer orderProducer;
    private final BankServiceImp bankService;
    @Value("${email.merchant}")
    private String merchantEmail;
    private final StockMessageSender stockMessageSender;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, String authToken) {
        log.info("Creating order for customer: {}", request.getCustomerEmail());

        if (authToken != null) {
            validateUserToken(authToken);
        }

        Order order = new Order();
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerName(request.getCustomerName());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(item -> createOrderItem(item, order))
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        BigDecimal orderTotal = calculateOrderTotal(orderItems);
        order.setOrderTotal(orderTotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;

        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            CouponValidationRequest validationRequest = new CouponValidationRequest(
                    request.getCouponCode(),
                    orderTotal,
                    request.getCustomerEmail()
            );

            CouponValidationResponse validationResponse = couponServiceClient.validateCoupon(validationRequest);

            if (validationResponse.isValid()) {
                discountAmount = validationResponse.getDiscountAmount();
                appliedCouponCode = request.getCouponCode();
                order.setAppliedCouponCode(appliedCouponCode);
                log.info("Applied coupon {} with discount: {}", request.getCouponCode(), discountAmount);
            } else {
                log.warn("Failed to apply coupon {}: Invalid coupon", request.getCouponCode());
                throw new RuntimeException("Coupon validation failed: Invalid coupon code");
            }
        }

        order.setDiscountAmount(discountAmount);
        order.calculateFinalAmount();

        Order savedOrder = orderRepository.save(order);

        if (appliedCouponCode != null) {
            CouponConsumeRequest consumeRequest = new CouponConsumeRequest(
                    appliedCouponCode,
                    order.getCustomerEmail(),
                    order.getId().toString()
            );
            couponServiceClient.consumeCoupon(consumeRequest);
        }

        Map<Long, Integer> products_quantities = new HashMap<>();
        for(OrderItem orderItem : savedOrder.getOrderItems()){
            products_quantities.put(orderItem.getProductId(), orderItem.getQuantity());
        }
        stockMessageSender.sendConsumeStockMessage(savedOrder.getId(), products_quantities);

        log.info("Created order with ID: {} for customer: {}", savedOrder.getId(), request.getCustomerEmail());
        return convertToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        return convertToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(String customerEmail) {
        return orderRepository.findByCustomerEmail(customerEmail).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderItem createOrderItem(OrderItemRequest request, Order order) {
        ProductDto product = getProductFromCatalogService(request.getProductId());

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductId(request.getProductId());
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(request.getQuantity());
        orderItem.setUnitPrice(product.getPrice());
        orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        return orderItem;
    }

    private BigDecimal calculateOrderTotal(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setCustomerName(order.getCustomerName());
        response.setOrderTotal(order.getOrderTotal());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setFinalAmount(order.getFinalAmount());
        response.setAppliedCouponCode(order.getAppliedCouponCode());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
        response.setOrderItems(itemResponses);

        return response;
    }

    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setProductId(orderItem.getProductId());
        response.setProductName(orderItem.getProductName());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setTotalPrice(orderItem.getTotalPrice());
        return response;
    }

    private ProductDto getProductFromCatalogService(Long productId) {
        try {
            CatalogResponse<ProductDto> catalogResponse = categoryServiceClient.getProductById(productId);
            if (!catalogResponse.isSuccess() || catalogResponse.getData() == null) {
                throw new RuntimeException("Product not found with ID: " + productId);
            }
            ProductDto product = catalogResponse.getData();
            log.info("Retrieved product details for ID: {} - Name: {}, Price: {}",
                    productId, product.getName(), product.getPrice());
            return product;
        } catch (Exception e) {
            log.error("Failed to retrieve product details for ID: {}", productId, e);
            throw new RuntimeException("Product retrieval failed: " + e.getMessage());
        }
    }

    private void validateUserToken(String authToken) {
        try {
            Boolean isValidUser = userServiceClient.checkUserRole(authToken, "USER");
            if (!Boolean.TRUE.equals(isValidUser)) {
                throw new RuntimeException("Invalid user authentication");
            }
            log.info("User authentication validated successfully");
        } catch (Exception e) {
            log.error("User authentication validation failed", e);
            throw new RuntimeException("User authentication validation failed: " + e.getMessage());
        }
    }






//    ----------------------Taghreed Adds------------------------

    private void applyDepositTransactionForMerchant(double amount) {
        TransactionRequest transactionRequestMerchant = new TransactionRequest();
        transactionRequestMerchant.setCardNumber("6656025033877442");
        transactionRequestMerchant.setAmount(amount);
        ResponseEntity<String> depositResponse = bankService.deposit(transactionRequestMerchant);

        // Check if deposit was successful
        if (!depositResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Merchant deposit failed: " + depositResponse.getBody());
        }

        log.info(" merchant deposit done");
    }

    private void applyWithdrawTransactionForCustomer(double amount, PaymentRequest orderModel) {
        TransactionRequest transactionRequestCustomer = new TransactionRequest();
        transactionRequestCustomer.setAmount(amount);
        transactionRequestCustomer.setCardNumber(orderModel.getCardNumber());
        ResponseEntity<String> withdrawResponse = bankService.withdraw(transactionRequestCustomer);
        // Check if withdrawal was successful
        if (!withdrawResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Customer withdrawal failed: " + withdrawResponse.getBody());
        }
        log.info("Customer withdrawal request processed successfully. Response: {}", withdrawResponse.getBody());
    }

    private void validateCardDetails(PaymentRequest request) {
        // Card number validation
        if (request.getCardNumber() == null || !request.getCardNumber().matches("^[0-9]{16}$")) {
            throw new RuntimeException("Invalid card number - must be 16 digits");
        }

        // Expiry date validation
        if (request.getExpiryDate() != null && !request.getExpiryDate().isEmpty()) {
            if (!request.getExpiryDate().matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
                throw new RuntimeException("Invalid expiry date format - use MM/YY");
            }

            String[] parts = request.getExpiryDate().split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]);

            LocalDate expiry = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
            if (expiry.isBefore(LocalDate.now())) {
                throw new RuntimeException("Card has expired");
            }
        }


        // CVV validation
        if (request.getCvv() != null && !request.getCvv().matches("^[0-9]{3,4}$")) {
            throw new RuntimeException("Invalid CVV - must be 3 or 4 digits");
        }
    }

    @Transactional
    public OrderResponse processPayment(Long orderId, PaymentRequest paymentRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        log.warn("Current order status: {}", order.getStatus());

        if (order.getStatus().equals(OrderStatus.PENDING)) {
            throw new RuntimeException("Order is not in a valid state for payment. Current status: " + order.getStatus());
        }

        validateCardDetails(paymentRequest);

        // احسبي الـ amount من order.getFinalAmount() مش من البودي
        double expectedAmount = order.getFinalAmount().doubleValue();
        paymentRequest.setAmount(expectedAmount); // حطيه بنفسك هنا
        paymentRequest.setOrderId(orderId); // تأكيد إضافي لو حصل لخبطة

        try {
            applyWithdrawTransactionForCustomer(expectedAmount, paymentRequest);
            log.info("Customer withdrawal successful for order: {}", orderId);

            applyDepositTransactionForMerchant(expectedAmount);
            log.info("Merchant deposit successful for order: {}", orderId);

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        } catch (Exception e) {
            log.error("Payment failed for order: {}", orderId, e);
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw new RuntimeException("Payment failed: " + e.getMessage());
        }

        return convertToOrderResponse(order);
    }



}