package com.systemsimulator.repository;

import com.systemsimulator.model.Component;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryComponentRepository {
    private final Map<String, Component> components = new ConcurrentHashMap<>();

    public Component save(Component component) {
        components.put(component.getId(), component);
        return component;
    }

    public Optional<Component> findById(String id) {
        return Optional.ofNullable(components.get(id));
    }

    public List<Component> findAll() {
        return new ArrayList<>(components.values());
    }

    public void deleteById(String id) {
        components.remove(id);
    }

    public boolean existsById(String id) {
        return components.containsKey(id);
    }

    public void deleteAll() {
        components.clear();
    }
}

