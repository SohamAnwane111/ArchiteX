package com.systemsimulator.model.rules;

import com.systemsimulator.model.*;

public class CacheLookupRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        if (linkType != LinkType.CACHE_LOOKUP) return false;

        // API Services can lookup Cache
        if (source instanceof APIServiceComponent && target instanceof CacheComponent) {
            return true;
        }

        // Load Balancers can lookup Cache (session affinity)
        if (source instanceof LoadBalancerComponent && target instanceof CacheComponent) {
            return true;
        }

        // Cache can fallback to Database (cache miss)
        if (source instanceof CacheComponent && target instanceof DatabaseComponent) {
            return true;
        }

        // Cache can fallback to Storage (for large objects)
        if (source instanceof CacheComponent && target instanceof StorageComponent) {
            return true;
        }

        // Stream Processors can use Cache for lookups
        if (source.getType() == ComponentType.STREAM_PROCESSOR && target instanceof CacheComponent) {
            return true;
        }

        return false;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.CACHE_LOOKUP;
    }

    @Override
    public String getDescription() {
        return "CACHE_LOOKUP: API->Cache, LoadBalancer->Cache, StreamProcessor->Cache, Cache->Database, Cache->Storage (caching patterns)";
    }
}

