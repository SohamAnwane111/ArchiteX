package com.systemsimulator.utils;

import com.systemsimulator.model.Parameter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ParameterWeights {
    private Map<Parameter, Double> defaultWeights = new HashMap<>();

    public ParameterWeights() {
        initializeDefaultWeights();
    }

    /**
     * Initialize default weights for each parameter
     * These weights determine how important each parameter is in overall scoring
     */
    private void initializeDefaultWeights() {
        defaultWeights.put(Parameter.LATENCY, 1.5);          // High priority
        defaultWeights.put(Parameter.THROUGHPUT, 1.2);       // Important for performance
        defaultWeights.put(Parameter.COST, 1.0);             // Moderate priority
        defaultWeights.put(Parameter.SCALABILITY, 1.2);      // Important for growth
        defaultWeights.put(Parameter.CONSISTENCY, 1.0);      // Moderate priority
        defaultWeights.put(Parameter.AVAILABILITY, 1.3);     // High priority for reliability
        defaultWeights.put(Parameter.DURABILITY, 1.0);       // Moderate priority
        defaultWeights.put(Parameter.MAINTAINABILITY, 0.8);  // Lower priority
        defaultWeights.put(Parameter.ENERGY_EFFICIENCY, 0.7); // Lower priority
        defaultWeights.put(Parameter.SECURITY, 1.1);         // Important for safety
    }

    /**
     * Get all default parameter weights
     */
    public Map<Parameter, Double> getDefaultWeights() {
        return new HashMap<>(defaultWeights);
    }

    /**
     * Set weight for a specific parameter
     */
    public void setWeight(Parameter parameter, double weight) {
        if (weight < 0.0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        defaultWeights.put(parameter, weight);
    }

    /**
     * Get weight for a specific parameter
     */
    public double getWeight(Parameter parameter) {
        return defaultWeights.getOrDefault(parameter, 1.0);
    }

    /**
     * Reset to default weights
     */
    public void resetToDefaults() {
        defaultWeights.clear();
        initializeDefaultWeights();
    }

    /**
     * Apply a custom weight profile (e.g., for performance-focused vs cost-focused architectures)
     */
    public void applyProfile(WeightProfile profile) {
        switch (profile) {
            case PERFORMANCE_FOCUSED:
                defaultWeights.put(Parameter.LATENCY, 2.0);
                defaultWeights.put(Parameter.THROUGHPUT, 2.0);
                defaultWeights.put(Parameter.SCALABILITY, 1.5);
                defaultWeights.put(Parameter.COST, 0.5);
                break;
            case COST_OPTIMIZED:
                defaultWeights.put(Parameter.COST, 2.0);
                defaultWeights.put(Parameter.ENERGY_EFFICIENCY, 1.5);
                defaultWeights.put(Parameter.LATENCY, 0.8);
                defaultWeights.put(Parameter.THROUGHPUT, 0.8);
                break;
            case RELIABILITY_FOCUSED:
                defaultWeights.put(Parameter.AVAILABILITY, 2.0);
                defaultWeights.put(Parameter.DURABILITY, 2.0);
                defaultWeights.put(Parameter.CONSISTENCY, 1.5);
                defaultWeights.put(Parameter.LATENCY, 0.8);
                break;
            case BALANCED:
            default:
                initializeDefaultWeights();
                break;
        }
    }

    /**
     * Weight profiles for different architecture priorities
     */
    public enum WeightProfile {
        BALANCED,           // Default balanced weights
        PERFORMANCE_FOCUSED, // Optimize for speed and throughput
        COST_OPTIMIZED,     // Minimize costs
        RELIABILITY_FOCUSED  // Maximize uptime and durability
    }
}

