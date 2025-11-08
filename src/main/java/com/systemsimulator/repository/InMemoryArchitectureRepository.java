package com.systemsimulator.repository;

import com.systemsimulator.model.Architecture;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryArchitectureRepository {
    private final Map<String, Architecture> architectures = new ConcurrentHashMap<>();

    public Architecture save(Architecture architecture) {
        architectures.put(architecture.getId(), architecture);
        return architecture;
    }

    public Optional<Architecture> findById(String id) {
        return Optional.ofNullable(architectures.get(id));
    }

    public List<Architecture> findAll() {
        return new ArrayList<>(architectures.values());
    }

    public void deleteById(String id) {
        architectures.remove(id);
    }

    public boolean existsById(String id) {
        return architectures.containsKey(id);
    }

    public void deleteAll() {
        architectures.clear();
    }
}

