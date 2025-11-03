package com.example.ecoms.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecoms.Client.PaymentClient;
import com.example.ecoms.DTO.OrderDTO;
import com.example.ecoms.DTO.OrderItemDTO;
import com.example.ecoms.DTO.PaymentRequestDTO;
import com.example.ecoms.DTO.PaymentResponseDTO;
import com.example.ecoms.Entity.OrderItem;
import com.example.ecoms.Entity.Orders;
import com.example.ecoms.Entity.Product;
import com.example.ecoms.Entity.Status;
import com.example.ecoms.Entity.User;
import com.example.ecoms.Repository.OrderRepository;
import com.example.ecoms.Repository.ProductRepository;
import com.example.ecoms.Repository.UserRepository;
import com.example.ecoms.Service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PaymentClient paymentClient;

    // =========================================
    // 🟢 Helper Method to Convert Entity → DTO
    // =========================================
    private OrderDTO convertToDTO(Orders order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setRazorpayKeyId(order.getRazorpayKeyId());
        dto.setPaymentId(order.getPaymentId());
        dto.setPaymentStatus(order.getPaymentStatus());

        // ✅ Set user info
        if (order.getUser() != null) {
            dto.setUserName(order.getUser().getName());
            dto.setEmail(order.getUser().getEmail());
        }

        // ✅ Set order items
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(item -> new OrderItemDTO(
                            item.getProduct() != null ? item.getProduct().getId() : null,
                            item.getProduct() != null ? item.getProduct().getName() : null,
                            item.getPrice(),
                            item.getQuantity()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // =========================================
    // 🟢 TEST FEIGN CONNECTION
    // =========================================
    public void testFeignConnection() {
        try {
            String result = paymentClient.feignTest();
            log.info("✅ FEIGN CLIENT WORKING: {}", result);
        } catch (Exception e) {
            log.error("❌ FEIGN CLIENT FAILED: {}", e.getMessage());
        }
    }

    // =========================================
    // 🟢 Place Order
    // =========================================
    @Override
    public OrderDTO placeOrder(Long userId, Map<Long, Integer> productQuantities, Double totalAmount) {
        try {
            log.info("🛒 Placing order for user ID: {}", userId);

            // 1️⃣ Validate user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            // 2️⃣ Create order entity
            Orders order = new Orders();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(Status.PENDING);
            order.setTotalAmount(new java.math.BigDecimal(totalAmount));

            // 3️⃣ Add order items
            List<OrderItem> orderItems = productQuantities.entrySet().stream()
                    .map(entry -> {
                        Product product = productRepository.findById(entry.getKey())
                                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + entry.getKey()));

                        OrderItem item = new OrderItem();
                        item.setOrder(order);
                        item.setProduct(product);
                        item.setQuantity(entry.getValue());
                        item.setPrice(product.getPrice());
                        return item;
                    })
                    .collect(Collectors.toList());

            order.setOrderItems(orderItems);
            Orders savedOrder = orderRepository.save(order);
            log.info("✅ Order saved successfully: {}", savedOrder.getId());

            // 4️⃣ 🚀 CRITICAL: Call Payment Service via Feign Client
            PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
            paymentRequest.setUserId(userId);
            paymentRequest.setOrderId(savedOrder.getId());
            paymentRequest.setAmount(totalAmount);
            paymentRequest.setCustomerEmail(user.getEmail());
            paymentRequest.setCustomerName(user.getName());
            paymentRequest.setDescription("Payment for Order #" + savedOrder.getId());

            log.info("📞 Calling Payment Service for order: {}", savedOrder.getId());
            PaymentResponseDTO paymentResponse = paymentClient.createPayment(paymentRequest);
            log.info("💳 Payment created successfully: {}", paymentResponse.getRazorpayOrderId());

            // 5️⃣ Update order with payment info
            savedOrder.setPaymentId(paymentResponse.getRazorpayOrderId());
            savedOrder.setPaymentStatus(paymentResponse.getStatus());
            savedOrder.setRazorpayKeyId(paymentResponse.getKeyId());
            orderRepository.save(savedOrder);

            return convertToDTO(savedOrder);

        } catch (Exception e) {
            log.error("❌ Failed to place order: {}", e.getMessage(), e);
            throw new RuntimeException("Order placement failed: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 Get All Orders - MISSING METHOD ADDED
    // =========================================
    @Override
    public List<OrderDTO> getAllOrders() {
        log.info("📦 Fetching all orders...");
        try {
            List<Orders> orders = orderRepository.findAllOrdersWithUser();
            log.info("✅ Found {} orders", orders.size());
            return orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Failed to fetch all orders: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch orders: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 Get Orders by User
    // =========================================
    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        log.info("📦 Fetching orders for user ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            List<Orders> userOrders = orderRepository.findByUser(user);
            log.info("✅ Found {} orders for user {}", userOrders.size(), userId);
            
            return userOrders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Failed to fetch orders for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to fetch user orders: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 Get Orders by Status
    // =========================================
    @Override
    public List<OrderDTO> findByOrderStatus(Status status) {
        log.info("🔍 Fetching orders by status: {}", status);
        try {
            List<Orders> orders = orderRepository.findByStatus(status);
            log.info("✅ Found {} orders with status {}", orders.size(), status);
            
            return orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Failed to fetch orders by status {}: {}", status, e.getMessage());
            throw new RuntimeException("Failed to fetch orders by status: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 Update Order Status
    // =========================================
    @Override
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        log.info("📝 Updating order {} to status {}", orderId, status);
        try {
            Orders order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

            Status newStatus;
            try {
                newStatus = Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid order status: " + status);
            }

            order.setStatus(newStatus);
            Orders updatedOrder = orderRepository.save(order);
            log.info("✅ Order {} status updated to {}", orderId, newStatus);
            
            return convertToDTO(updatedOrder);
        } catch (Exception e) {
            log.error("❌ Failed to update order {} status: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to update order status: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 Cancel Order
    // =========================================
    @Override
    public OrderDTO cancelOrder(Long orderId) {
        log.info("🚫 Cancelling order with ID: {}", orderId);
        try {
            Orders order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

            if (order.getStatus() == Status.DELIVERED || order.getStatus() == Status.CANCELLED) {
                throw new RuntimeException("Order cannot be cancelled (status: " + order.getStatus() + ")");
            }

            order.setStatus(Status.CANCELLED);
            Orders cancelledOrder = orderRepository.save(order);
            log.info("✅ Order {} cancelled successfully", orderId);
            
            return convertToDTO(cancelledOrder);
        } catch (Exception e) {
            log.error("❌ Failed to cancel order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to cancel order: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 Get Recent Orders
    // =========================================
    @Override
    public List<OrderDTO> getRecentOrders(int days) {
        log.info("📅 Fetching recent orders placed in the last {} days", days);
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            List<Orders> recentOrders = orderRepository.findRecentOrders(startDate);
            log.info("✅ Found {} recent orders from last {} days", recentOrders.size(), days);
            
            return recentOrders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Failed to fetch recent orders: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch recent orders: " + e.getMessage());
        }
    }
}