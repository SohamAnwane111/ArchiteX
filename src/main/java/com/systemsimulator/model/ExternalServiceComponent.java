package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExternalServiceComponent extends Component {

    public ExternalServiceComponent() {
        super();
    }

    public ExternalServiceComponent(String id, String name) {
        super(id, name);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.EXTERNAL_SERVICE;
    }
}
