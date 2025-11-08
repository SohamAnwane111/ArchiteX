package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class Link {
    private String id;
    private Component source;
    private Component target;
    private LinkType type;
    private HeuristicProfile heuristics = new HeuristicProfile();
    private Map<String, Object> properties = new HashMap<>();

    public Link() {}

    public Link(String id, Component source, Component target, LinkType type) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.type = type;
    }

}
