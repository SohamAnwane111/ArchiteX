package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CacheComponent extends Component {
    public enum CacheType {
        IN_MEMORY, DISTRIBUTED, LOCAL
    }

    private CacheType cacheType;

    public CacheComponent() {
        super();
    }

    public CacheComponent(String id, String name, CacheType cacheType) {
        super(id, name);
        this.cacheType = cacheType;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CACHE;
    }
}
