package com.archival.archivalservice.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.archival.archivalservice.repository", // Change to your package
        entityManagerFactoryRef = "appEntityManagerFactory",
        transactionManagerRef = "appTransactionManager"
)
public class ArchivalDatabaseConfig {

    @Bean(name = "archivalDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.archival")
    public DataSource archivalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "archivalEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
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
