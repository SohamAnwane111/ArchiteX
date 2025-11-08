package com.systemsimulator.model.rules;

import com.systemsimulator.model.*;

public class ReplicationRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        if (linkType != LinkType.REPLICATION) return false;

        // Database to Database replication (master-slave, master-master)
        if (source instanceof DatabaseComponent && target instanceof DatabaseComponent) {
            return true;
        }

        // Cache to Cache replication (distributed cache clusters)
        if (source instanceof CacheComponent && target instanceof CacheComponent) {
            return true;
        }

        // Storage to Storage replication (backup, geo-redundancy)
        if (source instanceof StorageComponent && target instanceof StorageComponent) {
            return true;
        }

        // Queue to Queue replication (for high availability)
        if (source instanceof QueueComponent && target instanceof QueueComponent) {
            return true;
        }

        return false;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.REPLICATION;
    }

    @Override
    public String getDescription() {
        return "REPLICATION: Database->Database, Cache->Cache, Storage->Storage, Queue->Queue (same-type replication for redundancy)";
    }
}

