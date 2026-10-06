package com.taxibooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Main Entry Point for Taxi & Cab Service Booking Platform
 * SE1020 - Object Oriented Programming Coursework
 */
@SpringBootApplication
public class TaxiBookingApplication {

    private static final Logger logger = LoggerFactory.getLogger(TaxiBookingApplication.class);

    public static void main(String[] args) {
        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            logger.info("Data directory created: {}", created);
        }

        SpringApplication.run(TaxiBookingApplication.class, args);
        logger.info("==========================================================");
        logger.info("🚖 Taxi Booking Service Application Started Successfully!");
        logger.info("🌐 Web URL: http://localhost:8080");
        logger.info("==========================================================");
    }
}
