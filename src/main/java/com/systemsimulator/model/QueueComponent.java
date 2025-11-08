package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QueueComponent extends Component {
    public enum QueueType {
        MESSAGE_QUEUE, TASK_QUEUE, STREAM, EVENT_BUS
    }

    private QueueType queueType;

    public QueueComponent() {
        super();
    }

    public QueueComponent(String id, String name, QueueType queueType) {
        super(id, name);
        this.queueType = queueType;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.QUEUE;
    }
}
