package com.example.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * DataSource configuration for inventory_db.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.project.inventory.repository",
    entityManagerFactoryRef = "inventoryEntityManagerFactory",
    transactionManagerRef = "inventoryTransactionManager"
)
public class InventoryDataSourceConfig {

    @Value("${spring.datasource.inventory.url:jdbc:postgresql://localhost:5432/inventory_db}")
    private String url;

    @Value("${spring.datasource.inventory.username:project_user}")
    private String username;

    @Value("${spring.datasource.inventory.password:1234}")
    private String password;

    @Value("${spring.datasource.inventory.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    public DataSource inventoryDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean inventoryEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(inventoryDataSource());
        em.setPackagesToScan("com.example.project.inventory.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.ddl-auto", "update");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager inventoryTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(inventoryEntityManagerFactory().getObject());
        return transactionManager;
    }
}

