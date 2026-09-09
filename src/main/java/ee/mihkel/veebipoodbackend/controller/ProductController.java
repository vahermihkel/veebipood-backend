package ee.mihkel.veebipoodbackend.controller;

import ee.mihkel.veebipoodbackend.entity.Category;
import ee.mihkel.veebipoodbackend.entity.Product;
import ee.mihkel.veebipoodbackend.repository.CategoryRepository;
import ee.mihkel.veebipoodbackend.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
        product.setId(null);
        if (!resolveCategory(product)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(productRepository.save(product));
    }

    @GetMapping
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @Valid @RequestBody Product updated) {
        if (!resolveCategory(updated)) {
            return ResponseEntity.badRequest().build();
        }
        return productRepository.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setDescription(updated.getDescription());
                    existing.setPrice(updated.getPrice());
                    existing.setStock(updated.getStock());
                    existing.setCategory(updated.getCategory());
                    existing.setImage(updated.getImage());
                    existing.setActive(updated.isActive());
                    return ResponseEntity.ok(productRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean resolveCategory(Product product) {
        Category category = product.getCategory();
        if (category == null || category.getId() == null) {
            product.setCategory(null);
            return true;
        }
        return categoryRepository.findById(category.getId())
                .map(found -> {
                    product.setCategory(found);
                    return true;
                })
                .orElse(false);
    }
}