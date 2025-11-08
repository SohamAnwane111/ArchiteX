package com.systemsimulator.model.rules;

import com.systemsimulator.model.*;

public class EtlPipelineRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        if (linkType != LinkType.ETL_PIPELINE) return false;

        // Batch Processors extract from: Databases, Storage, External Services
        if (source.getType() == ComponentType.BATCH_PROCESSOR) {
            return target instanceof DatabaseComponent ||
                   target instanceof StorageComponent ||
                   target.getType() == ComponentType.EXTERNAL_SERVICE;
        }

        // Data sources for ETL: Databases, Storage, External Services -> Batch Processor
        if (source instanceof DatabaseComponent ||
            source instanceof StorageComponent ||
            source.getType() == ComponentType.EXTERNAL_SERVICE) {
            return target.getType() == ComponentType.BATCH_PROCESSOR;
        }

        // Database to Database ETL (data migration, analytics)
        if (source instanceof DatabaseComponent && target instanceof DatabaseComponent) {
            return true;
        }

        // Storage to Database ETL (data ingestion)
        if (source instanceof StorageComponent && target instanceof DatabaseComponent) {
            return true;
        }

        // Database to Storage ETL (data archival, export)
        if (source instanceof DatabaseComponent && target instanceof StorageComponent) {
            return true;
        }

        return false;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.ETL_PIPELINE;
    }

    @Override
    public String getDescription() {
        return "ETL_PIPELINE: Database->BatchProcessor, Storage->BatchProcessor, ExternalService->BatchProcessor, BatchProcessor->Database, BatchProcessor->Storage, Database->Database, Storage->Database, Database->Storage";
    }
}

