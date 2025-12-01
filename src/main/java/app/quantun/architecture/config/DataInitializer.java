package app.quantun.architecture.config;

import app.quantun.architecture.persistence.entity.CategoryJpaEntity;
import app.quantun.architecture.persistence.entity.ProductJpaEntity;
import app.quantun.architecture.persistence.repository.CategoryJpaRepository;
import app.quantun.architecture.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryJpaRepository categoryRepository;
    private final ProductJpaRepository productRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("Initializing computer store data...");
            initializeData();
            log.info("Computer store data initialization completed!");
        } else {
            log.info("Data already exists, skipping initialization.");
        }
    }

    private void initializeData() {
        // Create Categories
        CategoryJpaEntity laptops = createCategory(
                "Laptops",
                "Portable computers for work, gaming, and everyday use"
        );

        CategoryJpaEntity desktops = createCategory(
                "Desktop Computers",
                "High-performance desktop PCs for home and office"
        );

        CategoryJpaEntity components = createCategory(
                "Computer Components",
                "Internal hardware components for building and upgrading PCs"
        );

        CategoryJpaEntity peripherals = createCategory(
                "Peripherals",
                "Input and output devices including keyboards, mice, and monitors"
        );

        CategoryJpaEntity accessories = createCategory(
                "Accessories",
                "Computer accessories and enhancement products"
        );

        CategoryJpaEntity storage = createCategory(
                "Storage Devices",
                "Hard drives, SSDs, and external storage solutions"
        );

        // Create Products - Laptops
        createProduct("Dell XPS 15 Laptop",
                "15.6-inch premium laptop with Intel Core i7, 16GB RAM, 512GB SSD, NVIDIA GTX 1650 Ti graphics",
                new BigDecimal("1499.99"), 25, "https://example.com/images/dell-xps-15.jpg", laptops);

        createProduct("MacBook Pro 14-inch",
                "Apple M3 Pro chip, 18GB unified memory, 512GB SSD, Liquid Retina XDR display",
                new BigDecimal("1999.99"), 15, "https://example.com/images/macbook-pro-14.jpg", laptops);

        createProduct("ASUS ROG Gaming Laptop",
                "17.3-inch gaming laptop with AMD Ryzen 9, 32GB RAM, 1TB SSD, RTX 4070 graphics",
                new BigDecimal("2299.99"), 12, "https://example.com/images/asus-rog.jpg", laptops);

        createProduct("Lenovo ThinkPad X1 Carbon",
                "14-inch business ultrabook with Intel Core i5, 16GB RAM, 256GB SSD, lightweight design",
                new BigDecimal("1349.99"), 30, "https://example.com/images/thinkpad-x1.jpg", laptops);

        // Create Products - Desktop Computers
        createProduct("HP Pavilion Desktop",
                "Mid-tower desktop with Intel Core i5, 16GB RAM, 512GB SSD + 1TB HDD, integrated graphics",
                new BigDecimal("799.99"), 20, "https://example.com/images/hp-pavilion.jpg", desktops);

        createProduct("Custom Gaming PC - RTX 4080",
                "High-end gaming desktop with Intel Core i9, 64GB RAM, 2TB NVMe SSD, RTX 4080, RGB lighting",
                new BigDecimal("3499.99"), 8, "https://example.com/images/gaming-pc-4080.jpg", desktops);

        createProduct("iMac 24-inch",
                "All-in-one desktop with Apple M3 chip, 8GB RAM, 256GB SSD, 4.5K Retina display",
                new BigDecimal("1299.99"), 18, "https://example.com/images/imac-24.jpg", desktops);

        // Create Products - Computer Components
        createProduct("AMD Ryzen 9 7950X Processor",
                "16-core, 32-thread desktop processor with 5.7GHz max boost, AM5 socket",
                new BigDecimal("549.99"), 35, "https://example.com/images/ryzen-9-7950x.jpg", components);

        createProduct("NVIDIA GeForce RTX 4070 Ti",
                "12GB GDDR6X graphics card with DLSS 3, ray tracing, 4K gaming performance",
                new BigDecimal("799.99"), 22, "https://example.com/images/rtx-4070ti.jpg", components);

        createProduct("Corsair Vengeance DDR5 RAM",
                "32GB (2x16GB) DDR5-6000MHz desktop memory, optimized for Intel and AMD platforms",
                new BigDecimal("149.99"), 50, "https://example.com/images/corsair-vengeance-ddr5.jpg", components);

        createProduct("ASUS ROG Strix Motherboard",
                "ATX motherboard with AM5 socket, PCIe 5.0, WiFi 6E, RGB lighting, premium power delivery",
                new BigDecimal("349.99"), 28, "https://example.com/images/asus-rog-mobo.jpg", components);

        createProduct("EVGA 850W Power Supply",
                "850W 80+ Gold certified modular PSU, fully sleeved cables, 10-year warranty",
                new BigDecimal("129.99"), 40, "https://example.com/images/evga-psu-850w.jpg", components);

        // Create Products - Peripherals
        createProduct("Logitech MX Master 3S Mouse",
                "Wireless ergonomic mouse with 8K DPI sensor, quiet clicks, USB-C charging",
                new BigDecimal("99.99"), 60, "https://example.com/images/mx-master-3s.jpg", peripherals);

        createProduct("Corsair K95 RGB Mechanical Keyboard",
                "Full-size mechanical keyboard with Cherry MX switches, per-key RGB, aluminum frame",
                new BigDecimal("179.99"), 45, "https://example.com/images/corsair-k95.jpg", peripherals);

        createProduct("Dell UltraSharp 27-inch Monitor",
                "4K IPS monitor with USB-C connectivity, 99% sRGB color coverage, height adjustable stand",
                new BigDecimal("499.99"), 32, "https://example.com/images/dell-ultrasharp-27.jpg", peripherals);

        createProduct("LG 34-inch Ultrawide Gaming Monitor",
                "34-inch curved 1440p display, 144Hz refresh rate, 1ms response time, G-Sync compatible",
                new BigDecimal("599.99"), 20, "https://example.com/images/lg-ultrawide-34.jpg", peripherals);

        createProduct("HyperX Cloud II Gaming Headset",
                "Wired gaming headset with 7.1 surround sound, noise-canceling microphone, memory foam ear cups",
                new BigDecimal("99.99"), 55, "https://example.com/images/hyperx-cloud-ii.jpg", peripherals);

        // Create Products - Accessories
        createProduct("Cable Matters USB-C Hub",
                "7-in-1 USB-C hub with HDMI, USB 3.0, SD card reader, and 100W power delivery",
                new BigDecimal("49.99"), 80, "https://example.com/images/usbc-hub.jpg", accessories);

        createProduct("AmazonBasics HDMI Cable 6ft",
                "High-speed HDMI 2.0 cable supporting 4K@60Hz, Ethernet, and ARC",
                new BigDecimal("12.99"), 150, "https://example.com/images/hdmi-cable.jpg", accessories);

        createProduct("Anker PowerPort USB Charger",
                "6-port USB charging station with 60W total output, PowerIQ technology",
                new BigDecimal("39.99"), 70, "https://example.com/images/anker-powerport.jpg", accessories);

        createProduct("Belkin Surge Protector",
                "12-outlet surge protector with 8ft cord, 4320 joules protection, USB charging ports",
                new BigDecimal("34.99"), 65, "https://example.com/images/belkin-surge.jpg", accessories);

        // Create Products - Storage Devices
        createProduct("Samsung 990 PRO NVMe SSD 1TB",
                "PCIe 4.0 NVMe M.2 SSD with 7450 MB/s read speed, 6900 MB/s write speed",
                new BigDecimal("129.99"), 45, "https://example.com/images/samsung-990-pro.jpg", storage);

        createProduct("WD Black 4TB HDD",
                "7200 RPM internal hard drive with 256MB cache, optimized for gaming and creative work",
                new BigDecimal("119.99"), 38, "https://example.com/images/wd-black-4tb.jpg", storage);

        createProduct("Seagate Backup Plus 5TB External Drive",
                "Portable external hard drive with USB 3.0, automatic backup software included",
                new BigDecimal("129.99"), 50, "https://example.com/images/seagate-backup-5tb.jpg", storage);

        createProduct("SanDisk Extreme Portable SSD 2TB",
                "Rugged external SSD with 1050 MB/s read speed, IP55 water and dust resistance",
                new BigDecimal("189.99"), 42, "https://example.com/images/sandisk-extreme-2tb.jpg", storage);
    }

    private CategoryJpaEntity createCategory(String name, String description) {
        CategoryJpaEntity category = CategoryJpaEntity.builder()
                .name(name)
                .description(description)
                .active(true)
                .build();
        return categoryRepository.save(category);
    }

    private ProductJpaEntity createProduct(String name, String description, BigDecimal price,
                                             Integer stock, String imageUrl, CategoryJpaEntity category) {
        ProductJpaEntity product = ProductJpaEntity.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .imageUrl(imageUrl)
                .active(true)
                .category(category)
                .build();
        return productRepository.save(product);
    }
}
