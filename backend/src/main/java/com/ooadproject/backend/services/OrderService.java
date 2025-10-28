package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.CheckoutRequestDTO;
import com.ooadproject.backend.dto.OrderItemDTO;
import com.ooadproject.backend.dto.OrderResponseDTO;
import com.ooadproject.backend.entities.*;
import com.ooadproject.backend.repositories.OrderItemRepository;
import com.ooadproject.backend.repositories.OrderRepository;
import com.ooadproject.backend.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final EmailService emailService;

    @Transactional
    public Order createOrder(User user, CheckoutRequestDTO request) {
        Cart cart = cartService.getOrCreateCart(user);
        List<CartItem> cartItems = cart.getCartItems();

        System.out.println("🔄 Creating order for user: " + user.getUsername());
        System.out.println("🔄 Cart ID: " + cart.getCartId());
        System.out.println("🔄 Cart items: " + (cartItems != null ? cartItems.size() : "null"));

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Calculate total
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(totalPrice);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setContactNumber(request.getContactNumber());
        order.setDeliveryScheduledDate(LocalDate.now().plusDays(5)); // Default 5 days
        order.setStatus(Order.OrderStatus.Pending);

        order = orderRepository.save(order);

        // Create order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setPersonalizationDetails(cartItem.getPersonalizationDetails());
            orderItemRepository.save(orderItem);
        }

        // Decrement stock quantities for all products in the order
        System.out.println("🔄 Decrementing stock quantities for order: " + order.getOrderId());
        productService.decrementStockForOrder(cartItems);

        // Process payment
        Payment payment = paymentService.processPayment(order, request);

        if (payment.getStatus() == Payment.PaymentStatus.Completed) {
            order.setStatus(Order.OrderStatus.Confirmed);
            orderRepository.save(order);

            // Send confirmation email
            try {
                System.out.println("📧 Attempting to send order confirmation email...");
                emailService.sendOrderConfirmation(order);
                System.out.println("✅ Order confirmation email sent successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send order confirmation email: " + e.getMessage());
                System.err.println("⚠️ Order was still created successfully - email failure is non-critical");
            }

            // Send comprehensive order summary to both customer and admin
            try {
                System.out.println("📧 Attempting to send order summary email...");
                emailService.sendOrderSummary(order);
                System.out.println("✅ Order summary email sent successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send order summary email: " + e.getMessage());
            }
        }

        // Always clear after Orded
        cartService.clearCart(user);

        return order;
    }

    public List<OrderResponseDTO> getUserOrders(User user) {
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Integer orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return convertToDTO(order);
    }

    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByOrderByOrderDateDesc(pageable);
        return orders.map(this::convertToDTO);
    }

    @Transactional
    public Order updateOrderStatus(Integer orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        order = orderRepository.save(order);

        // Send status update email
        emailService.sendOrderStatusUpdate(order);

        // Send additional emails for important status changes
        if (status == Order.OrderStatus.Shipped) {
            emailService.sendDeliveryNotification(order);
        } else if (status == Order.OrderStatus.Cancelled) {
            emailService.sendOrderCancellation(order);
        }

        return order;
    }

    private OrderResponseDTO convertToDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerName(order.getUser().getUsername());
        dto.setCustomerEmail(order.getUser().getEmail());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDeliveryScheduledDate(order.getDeliveryScheduledDate());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setContactNumber(order.getContactNumber());

        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::convertOrderItemToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setItemId(item.getItemId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setPersonalizationDetails(item.getPersonalizationDetails());
        dto.setItemTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return dto;
    }
}