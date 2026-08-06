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
 * Multi-datasource configuration for Wallet Microservice.
 * Separates wallet_db database from project_db (main/auth database).
 *
 * This configuration enables:
 * - Separate entity manager for wallet entities
 * - Independent transactions for wallet operations
 * - Isolated wallet database schema
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.project.wallet.repository",
    entityManagerFactoryRef = "walletEntityManagerFactory",
    transactionManagerRef = "walletTransactionManager"
)
public class WalletDataSourceConfig {

    @Value("${spring.datasource.wallet.url:jdbc:postgresql://localhost:5432/wallet_db}")
    private String url;

    @Value("${spring.datasource.wallet.username:project_user}")
    private String username;

    @Value("${spring.datasource.wallet.password:1234}")
    private String password;

    @Value("${spring.datasource.wallet.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    /**
     * Creates a separate datasource for wallet_db.
     * Configured via application.properties: spring.datasource.wallet.*
     */
    @Bean
    public DataSource walletDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    /**
     * Creates EntityManagerFactory for wallet entities.
     * Points to wallet_db and manages Wallet and Transaction entities.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean walletEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(walletDataSource());
        em.setPackagesToScan("com.example.project.wallet.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.ddl-auto", "update");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        em.setJpaPropertyMap(properties);

        return em;
    }

    /**
     * Creates transaction manager for wallet database operations.
     */
    @Bean
    public PlatformTransactionManager walletTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(walletEntityManagerFactory().getObject());
        return transactionManager;
    }
}

