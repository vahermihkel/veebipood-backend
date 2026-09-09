package ee.mihkel.veebipoodbackend.repository;

import ee.mihkel.veebipoodbackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
