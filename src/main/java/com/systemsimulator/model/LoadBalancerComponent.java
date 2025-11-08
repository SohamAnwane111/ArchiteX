package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoadBalancerComponent extends Component {
    public enum LoadBalancerType {
        ROUND_ROBIN, LEAST_CONNECTIONS, IP_HASH, WEIGHTED
    }

    private LoadBalancerType lbType;

    public LoadBalancerComponent() {
        super();
    }

    public LoadBalancerComponent(String id, String name, LoadBalancerType lbType) {
        super(id, name);
        this.lbType = lbType;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.LOAD_BALANCER;
    }
}
