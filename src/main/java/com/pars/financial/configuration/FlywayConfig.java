package com.pars.financial.configuration;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    /**
     * Custom Flyway migration strategy that repairs checksums before migrating
     * This is useful when migration files are modified after being applied
     */
    @Bean
    @Profile("debug")
    public FlywayMigrationStrategy flywayMigrationStrategy(DataSource dataSource) {
        return flyway -> {
            try {
                // Repair checksums before migrating (fixes checksum mismatches)
                logger.info("Repairing Flyway checksums...");
                Flyway repairFlyway = Flyway.configure()
                        .dataSource(dataSource)
                        .locations(flyway.getConfiguration().getLocations())
                        .baselineOnMigrate(flyway.getConfiguration().isBaselineOnMigrate())
                        .load();
                repairFlyway.repair();
                logger.info("Flyway checksums repaired successfully");
                
                // Now run migrations
                flyway.migrate();
            } catch (Exception e) {
                logger.error("Error during Flyway migration: {}", e.getMessage(), e);
                throw e;
            }
        };
    }
}

