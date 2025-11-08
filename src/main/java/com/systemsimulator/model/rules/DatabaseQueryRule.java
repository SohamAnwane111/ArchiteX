package com.systemsimulator.model.rules;

import com.systemsimulator.model.*;

public class DatabaseQueryRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        if (linkType != LinkType.DATABASE_QUERY) return false;

        // API Services can query Databases
        if (source instanceof APIServiceComponent && target instanceof DatabaseComponent) {
            return true;
        }

        // Batch Processors can query Databases
        if (source.getType() == ComponentType.BATCH_PROCESSOR && target instanceof DatabaseComponent) {
            return true;
        }

        // Stream Processors can query Databases (enrichment)
        if (source.getType() == ComponentType.STREAM_PROCESSOR && target instanceof DatabaseComponent) {
            return true;
        }

        // Database to Database queries (federated queries, cross-DB joins)
        if (source instanceof DatabaseComponent && target instanceof DatabaseComponent) {
            return true;
        }

        // External Services can query Databases (analytics, reporting)
        if (source.getType() == ComponentType.EXTERNAL_SERVICE && target instanceof DatabaseComponent) {
            return true;
        }

        return false;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.DATABASE_QUERY;
    }

    @Override
    public String getDescription() {
        return "DATABASE_QUERY: API->Database, BatchProcessor->Database, StreamProcessor->Database, Database->Database, ExternalService->Database (read/write operations)";
    }
}

