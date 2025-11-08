package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StreamProcessorComponent extends Component {

    public StreamProcessorComponent() {
        super();
    }

    public StreamProcessorComponent(String id, String name) {
        super(id, name);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.STREAM_PROCESSOR;
    }
}
