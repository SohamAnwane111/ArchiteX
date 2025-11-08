package com.systemsimulator.config;

import com.systemsimulator.service.ArchitectureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MongoDBConnectionLogger {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConnectionLogger.class);

    @Autowired
    private ArchitectureService architectureService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("╔════════════════════════════════════════════════════════════╗");
        logger.info("║         APPLICATION STARTED - CHECKING MONGODB             ║");
        logger.info("╚════════════════════════════════════════════════════════════╝");

        boolean connected = architectureService.isMongoDBConnected();

        if (connected) {
            logger.info("╔════════════════════════════════════════════════════════════╗");
            logger.info("║  ✓ ✓ ✓  MONGODB CONNECTION SUCCESSFUL  ✓ ✓ ✓              ║");
            logger.info("╚════════════════════════════════════════════════════════════╝");
            architectureService.logMongoDBStats();
        } else {
            logger.error("╔════════════════════════════════════════════════════════════╗");
            logger.error("║  ✗ ✗ ✗  MONGODB CONNECTION FAILED  ✗ ✗ ✗                  ║");
            logger.error("║  Please check your connection string in application.properties");
            logger.error("╚════════════════════════════════════════════════════════════╝");
        }

        logger.info("");
        logger.info("Ready to accept requests on http://localhost:8080");
        logger.info("Architecture submission endpoint: POST /api/architecture/{id}/submit");
        logger.info("");
    }
}

