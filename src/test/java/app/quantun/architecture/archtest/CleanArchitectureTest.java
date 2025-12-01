package app.quantun.architecture.archtest;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@DisplayName("Clean Architecture Rules")
class CleanArchitectureTest {

    private static final String BASE_PACKAGE = "app.quantun.architecture";
    private static final String DOMAIN_ENTITY_PACKAGE = BASE_PACKAGE + ".entity..";
    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("Dependency Rule - Inward Only")
    class DependencyRule {

        @Test
        @DisplayName("Domain entities should not depend on Use Cases")
        void entitiesShouldNotDependOnUseCases() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_ENTITY_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage("..usecase..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain entities should not depend on Presentation or Persistence layers")
        void entitiesShouldNotDependOnOuterLayers() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_ENTITY_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..presentation..", "..persistence..", "..controller..", "..presenter..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain entities should not depend on Frameworks")
        void entitiesShouldNotDependOnFrameworks() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_ENTITY_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..config..", "org.springframework..")
                    .because("Domain entities are the innermost layer and must be framework-independent");

            rule.check(classes);
        }

        @Test
        @DisplayName("Use Cases should not depend on Presentation or Persistence layers")
        void useCasesShouldNotDependOnOuterLayers() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..usecase..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..presentation..", "..persistence..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Use Cases should not depend on Frameworks")
        void useCasesShouldNotDependOnFrameworks() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..usecase..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..config..", "org.springframework..")
                    .because("Use cases should be framework-independent");

            rule.check(classes);
        }

        @Test
        @DisplayName("Presentation layer should not depend on Persistence layer")
        void presentationShouldNotDependOnPersistence() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..presentation..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..persistence..")
                    .because("Presentation and Persistence are separate adapter layers");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventions {

        @Test
        @DisplayName("Controllers should be suffixed with Controller")
        void controllersShouldBeSuffixed() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..controller..")
                    .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .should().haveSimpleNameEndingWith("Controller");

            rule.check(classes);
        }

        @Test
        @DisplayName("Interactors should be suffixed with Interactor")
        void interactorsShouldBeSuffixed() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..usecase..")
                    .and().haveSimpleNameNotEndingWith("UseCase")
                    .and().haveSimpleNameNotEndingWith("Data")
                    .and().haveSimpleNameNotEndingWith("Criteria")
                    .and().haveSimpleNameNotEndingWith("Result")
                    .and().haveSimpleNameNotEndingWith("Gateway")
                    .and().areNotInterfaces()
                    .should().haveSimpleNameEndingWith("Interactor");

            rule.check(classes);
        }

        @Test
        @DisplayName("Gateways should be suffixed with Gateway")
        void gatewaysShouldBeSuffixed() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..usecase.gateway..")
                    .should().haveSimpleNameEndingWith("Gateway")
                    .orShould().haveSimpleNameEndingWith("Result");

            rule.check(classes);
        }

        @Test
        @DisplayName("Presenters should be suffixed with Presenter")
        void presentersShouldBeSuffixed() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..presenter..")
                    .should().haveSimpleNameEndingWith("Presenter");

            rule.check(classes);
        }

        @Test
        @DisplayName("JPA entities should be suffixed with JpaEntity")
        void jpaEntitiesShouldBeSuffixed() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..persistence.entity..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("JpaEntity");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Structural Rules")
    class StructuralRules {

        @Test
        @DisplayName("Gateways in use case layer should be interfaces")
        void gatewaysShouldBeInterfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..usecase.gateway..")
                    .and().haveSimpleNameEndingWith("Gateway")
                    .should().beInterfaces()
                    .because("Gateways define contracts, implementations live in outer layers");

            rule.check(classes);
        }

        @Test
        @DisplayName("Use Case interfaces should be suffixed with UseCase")
        void useCaseInterfacesShouldBeSuffixed() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..usecase..")
                    .and().areInterfaces()
                    .and().haveSimpleNameNotEndingWith("Gateway")
                    .should().haveSimpleNameEndingWith("UseCase");

            rule.check(classes);
        }

        @Test
        @DisplayName("No cycles between packages")
        void noCyclesBetweenPackages() {
            ArchRule rule = slices()
                    .matching(BASE_PACKAGE + ".(*)..")
                    .should().beFreeOfCycles();

            rule.check(classes);
        }

        @Test
        @DisplayName("Interactors should not have Spring annotations")
        void interactorsShouldNotHaveSpringAnnotations() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..usecase..")
                    .and().haveSimpleNameEndingWith("Interactor")
                    .should().beAnnotatedWith(org.springframework.stereotype.Service.class)
                    .orShould().beAnnotatedWith(org.springframework.stereotype.Component.class)
                    .because("Interactors are pure Java classes, wired manually in BeanConfiguration");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Data Transfer Rules")
    class DataTransferRules {

        @Test
        @DisplayName("Request DTOs should be in dto.request package")
        void requestDtosShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Request")
                    .and().resideInAPackage("..presentation..")
                    .should().resideInAPackage("..dto.request..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Response DTOs should be in dto.response package")
        void responseDtosShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Response")
                    .and().resideInAPackage("..presentation..")
                    .should().resideInAPackage("..dto.response..");

            rule.check(classes);
        }

        @Test
        @DisplayName("JPA entities should be in persistence.entity package")
        void jpaEntitiesShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("JpaEntity")
                    .should().resideInAPackage("..persistence.entity..");

            rule.check(classes);
        }
    }
}
