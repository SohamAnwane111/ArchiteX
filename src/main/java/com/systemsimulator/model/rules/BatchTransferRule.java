package com.systemsimulator.model.rules;

import com.systemsimulator.model.*;

public class BatchTransferRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        if (linkType != LinkType.BATCH_TRANSFER) return false;

        // Batch Processor to Storage (results, exports)
        if (source.getType() == ComponentType.BATCH_PROCESSOR) {
            return target instanceof StorageComponent ||
                   target instanceof DatabaseComponent;
        }

        // Storage to Storage batch transfers (backups, archival)
        if (source instanceof StorageComponent && target instanceof StorageComponent) {
            return true;
        }

        // Database to Storage batch exports
        if (source instanceof DatabaseComponent && target instanceof StorageComponent) {
            return true;
        }

        // Storage to Database batch imports
        if (source instanceof StorageComponent && target instanceof DatabaseComponent) {
            return true;
        }

        // External Service to Storage (bulk downloads)
        if (source.getType() == ComponentType.EXTERNAL_SERVICE) {
            return target instanceof StorageComponent;
        }

        return false;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.BATCH_TRANSFER;
    }

    @Override
    public String getDescription() {
        return "BATCH_TRANSFER: BatchProcessor->Storage, BatchProcessor->Database, Storage->Storage, Database->Storage, Storage->Database, ExternalService->Storage";
    }
}

