package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BatchProcessorComponent extends Component {

    public BatchProcessorComponent() {
        super();
    }

    public BatchProcessorComponent(String id, String name) {
        super(id, name);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.BATCH_PROCESSOR;
    }
}
