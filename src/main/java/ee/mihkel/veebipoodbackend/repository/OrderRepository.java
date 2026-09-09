package ee.mihkel.veebipoodbackend.repository;

import ee.mihkel.veebipoodbackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}