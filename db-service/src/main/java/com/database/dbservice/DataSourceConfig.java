package com.database.dbservice;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "appDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.app")
    public DataSource appDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "archivalDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.archival")
    public DataSource archivalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("appDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.archival.archivalservice.appmodels") // Change to your model package
                .persistenceUnit("appPU")
                .properties(Map.of("hibernate.hbm2ddl.auto", "update"))
                .build();
    }

    @Primary
    @Bean(name = "appTransactionManager")
    public PlatformTransactionManager archivalTransactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "archivalEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean archivalEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("archivalDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.archival.archivalservice.model") // Change to your model package
                .persistenceUnit("appPU")
                .build();
    }

    @Bean(name = "archivalTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("archivalEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

