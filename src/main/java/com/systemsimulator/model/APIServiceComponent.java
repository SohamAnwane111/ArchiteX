package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APIServiceComponent extends Component {
    public enum APIType {
        REST, GRAPHQL, GRPC, SOAP
    }

    private APIType apiType;

    public APIServiceComponent() {
        super();
    }

    public APIServiceComponent(String id, String name, APIType apiType) {
        super(id, name);
        this.apiType = apiType;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.API_SERVICE;
    }
}
