package com.systemsimulator.model;

public interface ConnectionRule {
    boolean isValid(Component source, Component target, LinkType linkType);
    LinkType getLinkType();
    String getDescription();
}

