package com.example.ecoms.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.ecoms.Entity.Orders;
import com.example.ecoms.Entity.Status;
import com.example.ecoms.Entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    // Fetch all orders along with user details (avoids N+1 problem)
    @Query("SELECT o FROM Orders o JOIN FETCH o.user")
    List<Orders> findAllOrdersWithUser();

    // Find all orders placed by a specific user
    List<Orders> findByUser(User user);

    // Find orders by their current status
    List<Orders> findByStatus(Status status);

    // Find orders placed after a specific date/time
    @Query("SELECT o FROM Orders o WHERE o.orderDate >= :startDate")
    List<Orders> findRecentOrders(LocalDateTime startDate);
}
