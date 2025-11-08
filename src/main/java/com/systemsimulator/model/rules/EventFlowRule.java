package com.systemsimulator.model.rules;

import com.systemsimulator.model.*;

public class EventFlowRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        if (linkType != LinkType.EVENT_FLOW) return false;

        // Event publishers: API Services, Stream Processors -> Queue
        if (source instanceof APIServiceComponent ||
            source.getType() == ComponentType.STREAM_PROCESSOR) {
            return target instanceof QueueComponent;
        }

        // Event consumers: Queue -> API Services, Stream Processors
        if (source instanceof QueueComponent) {
            return target instanceof APIServiceComponent ||
                   target.getType() == ComponentType.STREAM_PROCESSOR ||
                   target.getType() == ComponentType.BATCH_PROCESSOR;
        }

        // API Service to API Service event notifications
        if (source instanceof APIServiceComponent && target instanceof APIServiceComponent) {
            return true;
        }

        // Database change events -> Queue (CDC - Change Data Capture)
        if (source instanceof DatabaseComponent && target instanceof QueueComponent) {
            return true;
        }

        return false;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.EVENT_FLOW;
    }

    @Override
    public String getDescription() {
        return "EVENT_FLOW: API->Queue, StreamProcessor->Queue, Queue->API, Queue->StreamProcessor, Queue->BatchProcessor, API->API, Database->Queue (event-driven patterns)";
    }
}

