package app.quantun.architecture.config;

import app.quantun.architecture.usecase.category.GetAllCategoriesInteractor;
import app.quantun.architecture.usecase.category.GetAllCategoriesUseCase;
import app.quantun.architecture.usecase.gateway.CategoryGateway;
import app.quantun.architecture.usecase.gateway.OrderGateway;
import app.quantun.architecture.usecase.gateway.ProductGateway;
import app.quantun.architecture.usecase.order.CreateOrderInteractor;
import app.quantun.architecture.usecase.order.CreateOrderUseCase;
import app.quantun.architecture.usecase.product.SearchProductsInteractor;
import app.quantun.architecture.usecase.product.SearchProductsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for wiring use case beans.
 * This is where the Clean Architecture interactors are instantiated.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public GetAllCategoriesUseCase getAllCategoriesUseCase(CategoryGateway categoryGateway) {
        return new GetAllCategoriesInteractor(categoryGateway);
    }

    @Bean
    public SearchProductsUseCase searchProductsUseCase(ProductGateway productGateway,
                                                        CategoryGateway categoryGateway) {
        return new SearchProductsInteractor(productGateway, categoryGateway);
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(ProductGateway productGateway,
                                                  OrderGateway orderGateway) {
        return new CreateOrderInteractor(productGateway, orderGateway);
    }
}
