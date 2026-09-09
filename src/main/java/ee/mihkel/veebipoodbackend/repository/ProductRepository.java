package ee.mihkel.veebipoodbackend.repository;

import ee.mihkel.veebipoodbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}