package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.AdminDashboardStatsDTO;
import com.ooadproject.backend.dto.OrderItemDTO;
import com.ooadproject.backend.dto.OrderResponseDTO;
import com.ooadproject.backend.dto.OrderSummaryDTO;
import com.ooadproject.backend.dto.ProductSalesDTO;
import com.ooadproject.backend.dto.SalesReportDTO;
import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.entities.OrderItem;
import com.ooadproject.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

        private final OrderRepository orderRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;
        private final InventoryRepository inventoryRepository;
        private final OrderItemRepository orderItemRepository;

        public AdminDashboardStatsDTO getDashboardStats() {
                AdminDashboardStatsDTO stats = new AdminDashboardStatsDTO();

                // Order statistics
                stats.setTotalOrders(orderRepository.count());
                stats.setPendingOrders((long) orderRepository.findByStatus(Order.OrderStatus.Pending).size());
                stats.setConfirmedOrders((long) orderRepository.findByStatus(Order.OrderStatus.Confirmed).size());
                stats.setShippedOrders((long) orderRepository.findByStatus(Order.OrderStatus.Shipped).size());
                stats.setDeliveredOrders((long) orderRepository.findByStatus(Order.OrderStatus.Delivered).size());
                stats.setCancelledOrders((long) orderRepository.findByStatus(Order.OrderStatus.Cancelled).size());

                // Revenue statistics - sum all non-cancelled orders
                List<Order> allOrdersForRevenue = orderRepository.findAll();
                stats.setTotalRevenue(allOrdersForRevenue.stream()
                                .filter(order -> order.getStatus() != Order.OrderStatus.Cancelled)
                                .map(Order::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                // Today's revenue
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
                List<Order> todayOrders = orderRepository.findOrdersBetweenDates(startOfDay, endOfDay);
                stats.setTodayRevenue(todayOrders.stream()
                                .filter(order -> order.getStatus() != Order.OrderStatus.Cancelled)
                                .map(Order::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                // Weekly revenue
                LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
                List<Order> weeklyOrders = orderRepository.findOrdersBetweenDates(weekStart, endOfDay);
                stats.setWeeklyRevenue(weeklyOrders.stream()
                                .filter(order -> order.getStatus() == Order.OrderStatus.Delivered)
                                .map(Order::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                // Monthly revenue
                LocalDateTime monthStart = LocalDate.now().minusDays(30).atStartOfDay();
                List<Order> monthlyOrders = orderRepository.findOrdersBetweenDates(monthStart, endOfDay);
                stats.setMonthlyRevenue(monthlyOrders.stream()
                                .filter(order -> order.getStatus() == Order.OrderStatus.Delivered)
                                .map(Order::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                // Customer statistics
                stats.setTotalCustomers(userRepository.count());

                // Product statistics (derive from products to avoid empty inventory sync
                // issues)
                long totalProducts = productRepository.count();
                long lowStockFromProducts = productRepository.findLowStockProducts(5).size();
                long outOfStockFromProducts = productRepository.findAll().stream()
                                .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() == 0)
                                .count();

                stats.setTotalProducts(totalProducts);
                stats.setLowStockProducts(lowStockFromProducts);
                stats.setOutOfStockProducts(outOfStockFromProducts);
                stats.setActiveProducts(totalProducts - outOfStockFromProducts);

                return stats;
        }

        public List<SalesReportDTO> getSalesReport(LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);

                List<Order> orders = orderRepository.findOrdersBetweenDates(start, end);

                // Group orders by date
                Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                                .filter(order -> order.getStatus() == Order.OrderStatus.Delivered)
                                .collect(Collectors.groupingBy(order -> order.getOrderDate().toLocalDate()));

                // Create report for each date
                List<SalesReportDTO> reports = new ArrayList<>();
                for (Map.Entry<LocalDate, List<Order>> entry : ordersByDate.entrySet()) {
                        LocalDate date = entry.getKey();
                        List<Order> dayOrders = entry.getValue();

                        SalesReportDTO report = new SalesReportDTO();
                        report.setDate(date);
                        report.setOrderCount((long) dayOrders.size());

                        BigDecimal totalSales = dayOrders.stream()
                                        .map(Order::getTotalPrice)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        report.setTotalSales(totalSales);

                        if (dayOrders.size() > 0) {
                                report.setAverageOrderValue(
                                                totalSales.divide(BigDecimal.valueOf(dayOrders.size()), 2,
                                                                RoundingMode.HALF_UP));
                        } else {
                                report.setAverageOrderValue(BigDecimal.ZERO);
                        }

                        reports.add(report);
                }

                // Sort by date
                reports.sort((a, b) -> a.getDate().compareTo(b.getDate()));

                return reports;
        }

        public List<ProductSalesDTO> getTopSellingProducts(int limit) {
                List<OrderItem> allOrderItems = orderItemRepository.findAll();

                // Group by product and calculate statistics
                Map<Integer, List<OrderItem>> itemsByProduct = allOrderItems.stream()
                                .collect(Collectors.groupingBy(item -> item.getProduct().getProductId()));

                List<ProductSalesDTO> productSales = new ArrayList<>();

                for (Map.Entry<Integer, List<OrderItem>> entry : itemsByProduct.entrySet()) {
                        List<OrderItem> items = entry.getValue();

                        ProductSalesDTO salesDTO = new ProductSalesDTO();
                        salesDTO.setProductId(entry.getKey());
                        salesDTO.setProductName(items.get(0).getProduct().getName());
                        salesDTO.setCategoryName(
                                        items.get(0).getProduct().getCategory() != null
                                                        ? items.get(0).getProduct().getCategory().getName()
                                                        : "Unknown");

                        Long totalQuantity = items.stream()
                                        .mapToLong(OrderItem::getQuantity)
                                        .sum();
                        salesDTO.setQuantitySold(totalQuantity);

                        BigDecimal totalRevenue = items.stream()
                                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        salesDTO.setTotalRevenue(totalRevenue);

                        if (totalQuantity > 0) {
                                salesDTO.setAveragePrice(
                                                totalRevenue.divide(BigDecimal.valueOf(totalQuantity), 2,
                                                                RoundingMode.HALF_UP));
                        } else {
                                salesDTO.setAveragePrice(BigDecimal.ZERO);
                        }

                        productSales.add(salesDTO);
                }

                // Sort by quantity sold and limit results
                return productSales.stream()
                                .sorted((a, b) -> Long.compare(b.getQuantitySold(), a.getQuantitySold()))
                                .limit(limit)
                                .collect(Collectors.toList());
        }

        public List<OrderSummaryDTO> getRecentOrders(int limit) {
                List<Order> orders = orderRepository.findAll();

                return orders.stream()
                                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                                .limit(limit)
                                .map(order -> {
                                        OrderSummaryDTO dto = new OrderSummaryDTO();
                                        dto.setOrderId(order.getOrderId());
                                        dto.setCustomerName(order.getUser().getUsername());
                                        dto.setOrderDate(order.getOrderDate());
                                        dto.setStatus(order.getStatus().name());
                                        dto.setTotalPrice(order.getTotalPrice());
                                        dto.setItemCount(order.getOrderItems() != null ? order.getOrderItems().size()
                                                        : 0);
                                        return dto;
                                })
                                .collect(Collectors.toList());
        }

        public List<OrderResponseDTO> getRecentOrdersDetailed(int limit) {
                List<Order> orders = orderRepository.findAll();

                return orders.stream()
                                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                                .limit(limit)
                                .map(this::convertToOrderResponseDTO)
                                .collect(Collectors.toList());
        }

        private OrderResponseDTO convertToOrderResponseDTO(Order order) {
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
                dto.setItemTotal(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
                return dto;
        }
}