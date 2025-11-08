package com.systemsimulator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.systemsimulator.model.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class HeuristicService {

    private Map<String, Map<String, Map<String, Double>>> heuristicsConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        loadHeuristicsFromJson();
    }

    /**
     * Load heuristics configuration from JSON file
     */
    private void loadHeuristicsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("heuristics-config.json");
            // Use TypeReference for simpler type mapping
            heuristicsConfig = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<Map<String, Map<String, Map<String, Double>>>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load heuristics configuration", e);
        }
    }

    /**
     * Get default heuristic scores for a component type
     */
    public HeuristicProfile getDefaultHeuristicsForType(ComponentType type) {
        return getHeuristicsForTypeAndSubtype(type, "default");
    }

    /**
     * Get heuristic scores for a specific component type and subtype
     */
    public HeuristicProfile getHeuristicsForTypeAndSubtype(ComponentType type, String subtype) {
        Map<Parameter, Double> scores = new HashMap<>();

        String typeKey = type.name();
        Map<String, Map<String, Double>> typeConfig = heuristicsConfig.get(typeKey);

        if (typeConfig == null) {
            // Return default neutral scores if type not found
            for (Parameter param : Parameter.values()) {
                scores.put(param, 5.0);
            }
            return new HeuristicProfile(scores);
        }

        // Try to get specific subtype, fall back to "default"
        Map<String, Double> subtypeConfig = typeConfig.get(subtype);
        if (subtypeConfig == null) {
            subtypeConfig = typeConfig.get("default");
        }

        if (subtypeConfig == null) {
            // Return neutral scores if no config found
            for (Parameter param : Parameter.values()) {
                scores.put(param, 5.0);
            }
            return new HeuristicProfile(scores);
        }

        // Convert string keys to Parameter enum
        for (Map.Entry<String, Double> entry : subtypeConfig.entrySet()) {
            try {
                Parameter param = Parameter.valueOf(entry.getKey());
                scores.put(param, entry.getValue());
            } catch (IllegalArgumentException e) {
                // Skip unknown parameters
            }
        }

        return new HeuristicProfile(scores);
    }

    /**
     * Get heuristics for a component with automatic subtype detection
     */
    public HeuristicProfile getHeuristicsForComponent(Component component) {
        String subtype = detectSubtype(component);
        return getHeuristicsForTypeAndSubtype(component.getType(), subtype);
    }

    /**
     * Detect the subtype of a component
     */
    private String detectSubtype(Component component) {
        if (component instanceof DatabaseComponent) {
            DatabaseComponent db = (DatabaseComponent) component;
            return db.getDatabaseType() != null ? db.getDatabaseType().name() : "default";
        } else if (component instanceof CacheComponent) {
            CacheComponent cache = (CacheComponent) component;
            return cache.getCacheType() != null ? cache.getCacheType().name() : "default";
        } else if (component instanceof APIServiceComponent) {
            APIServiceComponent api = (APIServiceComponent) component;
            return api.getApiType() != null ? api.getApiType().name() : "default";
        } else if (component instanceof QueueComponent) {
            QueueComponent queue = (QueueComponent) component;
            return queue.getQueueType() != null ? queue.getQueueType().name() : "default";
        } else if (component instanceof StorageComponent) {
            StorageComponent storage = (StorageComponent) component;
            return storage.getStorageType() != null ? storage.getStorageType().name() : "default";
        } else if (component instanceof LoadBalancerComponent) {
            LoadBalancerComponent lb = (LoadBalancerComponent) component;
            return lb.getLbType() != null ? lb.getLbType().name() : "default";
        }
        return "default";
    }

    /**
     * Calculate weighted score for a specific component
     */
    public double calculateComponentScore(Component component, Map<Parameter, Double> weights) {
        return component.getHeuristics().getWeightedScore(weights);
    }

    /**
     * Get default heuristic scores for a link type
     */
    public HeuristicProfile getDefaultHeuristicsForLinkType(LinkType linkType) {
        Map<Parameter, Double> scores = new HashMap<>();

        Map<String, Map<String, Double>> linksConfig = heuristicsConfig.get("LINKS");

        if (linksConfig == null) {
            // Return default neutral scores if LINKS config not found
            for (Parameter param : Parameter.values()) {
                scores.put(param, 5.0);
            }
            return new HeuristicProfile(scores);
        }

        Map<String, Double> linkTypeConfig = linksConfig.get(linkType.name());

        if (linkTypeConfig == null) {
            // Return neutral scores if link type not found
            for (Parameter param : Parameter.values()) {
                scores.put(param, 5.0);
            }
            return new HeuristicProfile(scores);
        }

        // Convert string keys to Parameter enum
        for (Map.Entry<String, Double> entry : linkTypeConfig.entrySet()) {
            try {
                Parameter param = Parameter.valueOf(entry.getKey());
                scores.put(param, entry.getValue());
            } catch (IllegalArgumentException e) {
                // Skip unknown parameters
            }
        }

        return new HeuristicProfile(scores);
    }

    /**
     * Get heuristics for a link with automatic type detection
     */
    public HeuristicProfile getHeuristicsForLink(Link link) {
        return getDefaultHeuristicsForLinkType(link.getType());
    }

    /**
     * Calculate weighted score for a specific link
     */
    public double calculateLinkScore(Link link, Map<Parameter, Double> weights) {
        return link.getHeuristics().getWeightedScore(weights);
    }

    /**
     * Update heuristic score for a link parameter
     */
    public void updateLinkHeuristicScore(Link link, Parameter parameter, double score) {
        if (score < 0.0 || score > 10.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 10.0");
        }
        link.getHeuristics().setScore(parameter, score);
    }

    /**
     * Update heuristic score for a specific parameter
     */
    public void updateHeuristicScore(Component component, Parameter parameter, double score) {
        if (score < 0.0 || score > 10.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 10.0");
        }
        component.getHeuristics().setScore(parameter, score);
    }

    /**
     * Get heuristics for a component with custom adjustments based on properties
     */
    public HeuristicProfile getAdjustedHeuristics(ComponentType type, Map<String, Object> properties) {
        HeuristicProfile baseProfile = getDefaultHeuristicsForType(type);

        // Apply adjustments based on properties
        if (properties != null && !properties.isEmpty()) {
            applyPropertyAdjustments(baseProfile, properties);
        }

        return baseProfile;
    }

    /**
     * Apply property-based adjustments to heuristics
     */
    private void applyPropertyAdjustments(HeuristicProfile profile, Map<String, Object> properties) {
        // Adjust based on replicas
        if (properties.containsKey("replicas")) {
            int replicas = getIntValue(properties.get("replicas"));
            if (replicas > 1) {
                adjustScore(profile, Parameter.AVAILABILITY, 1.0);
                adjustScore(profile, Parameter.DURABILITY, 0.5);
                adjustScore(profile, Parameter.COST, -0.5 * replicas);
            }
        }

        // Adjust based on memory/storage size
        if (properties.containsKey("memoryGB") || properties.containsKey("storageGB")) {
            adjustScore(profile, Parameter.COST, -0.5);
            adjustScore(profile, Parameter.THROUGHPUT, 0.5);
        }

        // Adjust based on instances
        if (properties.containsKey("instances")) {
            int instances = getIntValue(properties.get("instances"));
            if (instances > 1) {
                adjustScore(profile, Parameter.SCALABILITY, 1.0);
                adjustScore(profile, Parameter.AVAILABILITY, 0.5);
                adjustScore(profile, Parameter.COST, -0.3 * instances);
            }
        }
    }

    /**
     * Adjust a parameter score with bounds checking
     */
    private void adjustScore(HeuristicProfile profile, Parameter param, double adjustment) {
        double currentScore = profile.getScore(param);
        double newScore = Math.max(0.0, Math.min(10.0, currentScore + adjustment));
        profile.setScore(param, newScore);
    }

    /**
     * Get integer value from object
     */
    private int getIntValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    /**
     * Reload heuristics configuration from JSON (useful for runtime updates)
     */
    public void reloadConfiguration() {
        loadHeuristicsFromJson();
    }

    /**
     * Get all available subtypes for a component type
     */
    public Map<String, Map<String, Double>> getAvailableSubtypes(ComponentType type) {
        return heuristicsConfig.getOrDefault(type.name(), new HashMap<>());
    }
}
