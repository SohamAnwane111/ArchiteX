package com.systemsimulator.model.rules;

import com.systemsimulator.model.*;

public class ApiCallRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        if (linkType != LinkType.API_CALL) return false;

        // API Services can call: Databases, Caches, Queues, other API Services, External Services
        if (source instanceof APIServiceComponent) {
            return target instanceof DatabaseComponent ||
                   target instanceof CacheComponent ||
                   target instanceof QueueComponent ||
                   target instanceof APIServiceComponent ||
                   target instanceof StorageComponent;
        }

        // Load Balancers can call API Services
        if (source instanceof LoadBalancerComponent) {
            return target instanceof APIServiceComponent;
        }

        // Clients can call Load Balancers or API Services
        if (source.getType() == ComponentType.CLIENT) {
            return target instanceof LoadBalancerComponent ||
                   target instanceof APIServiceComponent;
        }

        return false;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.API_CALL;
    }

    @Override
    public String getDescription() {
        return "API_CALL: Client->LoadBalancer, Client->API, LoadBalancer->API, API->Database, API->Cache, API->Queue, API->Storage, API->API";
    }
}
