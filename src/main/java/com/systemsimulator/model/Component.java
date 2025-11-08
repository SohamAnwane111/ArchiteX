package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public abstract class Component {
    private String id;
    private String name;
    private HeuristicProfile heuristics = new HeuristicProfile();
    private Map<String, Object> properties = new HashMap<>();

    public Component() {}

    public Component(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Each concrete class must define its component type
    public abstract ComponentType getType();
}
