package com.database.dbservice;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway sourceFlyway(@Qualifier("appDataSource") DataSource sourceDataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(sourceDataSource)
                .baselineOnMigrate(true)
                .locations("classpath:db/migration/source")
                .load();
        flyway.migrate();
        return flyway;

    }

    @Bean
    public Flyway archivalFlyway(@Qualifier("archivalDataSource") DataSource archivalDataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(archivalDataSource)
                .baselineOnMigrate(true)
                .locations("classpath:db/migration/archival")
                .load();
        flyway.migrate();
        return flyway;
    }


}

