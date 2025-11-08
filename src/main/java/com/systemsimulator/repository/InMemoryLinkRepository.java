package com.systemsimulator.repository;

import com.systemsimulator.model.Link;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLinkRepository {
    private final Map<String, Link> links = new ConcurrentHashMap<>();

    public Link save(Link link) {
        links.put(link.getId(), link);
        return link;
    }

    public Optional<Link> findById(String id) {
        return Optional.ofNullable(links.get(id));
    }

    public List<Link> findAll() {
        return new ArrayList<>(links.values());
    }

    public void deleteById(String id) {
        links.remove(id);
    }

    public boolean existsById(String id) {
        return links.containsKey(id);
    }

    public void deleteAll() {
        links.clear();
    }

    public List<Link> findBySourceId(String sourceId) {
        List<Link> result = new ArrayList<>();
        for (Link link : links.values()) {
            if (link.getSource() != null && link.getSource().getId().equals(sourceId)) {
                result.add(link);
            }
        }
        return result;
    }

    public List<Link> findByTargetId(String targetId) {
        List<Link> result = new ArrayList<>();
        for (Link link : links.values()) {
            if (link.getTarget() != null && link.getTarget().getId().equals(targetId)) {
                result.add(link);
            }
        }
        return result;
    }
}

