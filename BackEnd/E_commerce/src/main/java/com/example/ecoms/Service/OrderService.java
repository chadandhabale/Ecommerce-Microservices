package com.example.ecoms.Service;

import java.util.List;
import java.util.Map;

import com.example.ecoms.DTO.OrderDTO;
import com.example.ecoms.Entity.Status;  

public interface OrderService {

	 OrderDTO placeOrder(Long userId, Map<Long, Integer> productQuantities, Double totalAmount);

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrdersByUserId(Long userId);

    List<OrderDTO> findByOrderStatus(Status status); 
    OrderDTO updateOrderStatus(Long orderId, String status);

    OrderDTO cancelOrder(Long orderId);

    List<OrderDTO> getRecentOrders(int days);
}
