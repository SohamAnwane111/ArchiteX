package com.systemsimulator.model.rules;

import com.systemsimulator.model.*;

public class StreamRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        if (linkType != LinkType.STREAM) return false;

        // Producers: API Services, Stream Processors can write to Queues
        if (source instanceof APIServiceComponent ||
            source.getType() == ComponentType.STREAM_PROCESSOR) {
            return target instanceof QueueComponent;
        }

        // Consumers: Stream Processors, API Services can read from Queues
        if (source instanceof QueueComponent) {
            return target instanceof APIServiceComponent ||
                   target.getType() == ComponentType.STREAM_PROCESSOR ||
                   target instanceof DatabaseComponent; // For stream sinks
        }

        // Stream Processors can chain to other Stream Processors
        if (source.getType() == ComponentType.STREAM_PROCESSOR) {
            return target.getType() == ComponentType.STREAM_PROCESSOR ||
                   target instanceof DatabaseComponent ||
                   target instanceof StorageComponent;
        }

        return false;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.STREAM;
    }

    @Override
    public String getDescription() {
        return "STREAM: API->Queue, StreamProcessor->Queue, Queue->API, Queue->StreamProcessor, Queue->Database, StreamProcessor->StreamProcessor, StreamProcessor->Database, StreamProcessor->Storage";
    }
}

