package app.quantun.architecture;

import app.quantun.architecture.domain.Category;
import app.quantun.architecture.domain.Product;
import app.quantun.architecture.repository.CategoryRepository;
import app.quantun.architecture.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ProductOptimisticLockingTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testOptimisticLocking() {
        // 1. Setup Data
        Category category = Category.builder()
                .name("Test Category")
                .active(true)
                .build();
        category = categoryRepository.save(category);

        Product product = Product.builder()
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .category(category)
                .active(true)
                .build();
        product = productRepository.save(product);

        // Ensure everything is flushed to DB
        productRepository.flush();
        entityManager.clear(); // Detach everything

        // 2. Simulate User A reading the product
        Product userAProduct = productRepository.findById(product.getId()).orElseThrow();

        // 3. Simulate User B reading the same product (in a real app, this would be a separate thread/request)
        // Since we cleared the EntityManager, this fetches a fresh instance
        entityManager.clear();
        Product userBProduct = productRepository.findById(product.getId()).orElseThrow();

        // Detach User B's product so it doesn't get updated by User A's save via merge
        entityManager.clear();

        // 4. User A updates stock
        userAProduct.setStock(userAProduct.getStock() - 1);
        productRepository.save(userAProduct);
        productRepository.flush();

        // 5. User B tries to update stock (using stale data)
        userBProduct.setStock(userBProduct.getStock() - 2);

        // This should fail with ObjectOptimisticLockingFailureException if @Version is present
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            productRepository.save(userBProduct);
            productRepository.flush(); // Flush to trigger the update and the version check
        });
    }
}
