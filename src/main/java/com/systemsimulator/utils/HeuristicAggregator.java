package com.systemsimulator.utils;

import com.systemsimulator.model.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HeuristicAggregator {

    /**
     * Aggregates heuristics parameter-wise with specific aggregation logic for each parameter.
     * Different parameters have different aggregation strategies:
     * - LATENCY, COST: Additive (compounds across architecture)
     * - AVAILABILITY, CONSISTENCY, SECURITY, DURABILITY: Minimum (weakest link)
     * - SCALABILITY, THROUGHPUT, MAINTAINABILITY, ENERGY_EFFICIENCY: Average
     */
    public Map<Parameter, Double> aggregateByParameter(
            List<com.systemsimulator.model.Component> components,
            List<Link> links) {

        Map<Parameter, Double> aggregated = new HashMap<>();

        if (components == null || components.isEmpty()) {
            return aggregated;
        }

        for (Parameter param : Parameter.values()) {
            double aggregatedValue = aggregateParameter(param, components, links);
            aggregated.put(param, aggregatedValue);
        }

        return aggregated;
    }

    /**
     * Aggregate a specific parameter based on its nature
     */
    private double aggregateParameter(Parameter param,
                                     List<com.systemsimulator.model.Component> components,
                                     List<Link> links) {
        switch (param) {
            case LATENCY:
                return aggregateLatency(components, links);

            case COST:
                return aggregateCost(components);

            case AVAILABILITY:
            case CONSISTENCY:
            case SECURITY:
            case DURABILITY:
                return aggregateMinimum(param, components);

            case SCALABILITY:
            case THROUGHPUT:
            case MAINTAINABILITY:
            case ENERGY_EFFICIENCY:
                return aggregateAverage(param, components);

            default:
                return aggregateAverage(param, components);
        }
    }

    /**
     * Aggregate LATENCY - Additive with path consideration
     * In a request path, latencies add up. We estimate worst-case latency.
     */
    private double aggregateLatency(List<com.systemsimulator.model.Component> components,
                                    List<Link> links) {
        if (components.isEmpty()) {
            return 10.0; // Perfect score
        }

        // Calculate average component latency (inverted score: lower is better)
        double totalLatencyScore = 0.0;
        int count = 0;

        for (com.systemsimulator.model.Component component : components) {
            Double score = component.getHeuristics().getScore(Parameter.LATENCY);
            if (score != null) {
                // Convert score to latency contribution (10 - score gives penalty)
                totalLatencyScore += (10.0 - score);
                count++;
            }
        }

        // Add link latency penalties
        if (links != null && !links.isEmpty()) {
            for (Link link : links) {
                // Each link adds latency overhead
                totalLatencyScore += (10.0 - link.getHeuristics().getScore(Parameter.LATENCY));
            }
            count += links.size();
        }

        // Convert back to score (higher is better)
        double avgLatencyPenalty = count > 0 ? totalLatencyScore / count : 0;
        double finalScore = Math.max(1.0, 10.0 - avgLatencyPenalty);

        // Apply path depth penalty (more hops = more latency)
        if (links != null && components.size() > 1) {
            double pathFactor = Math.max(0.7, 1.0 - (links.size() / (double) components.size() * 0.1));
            finalScore *= pathFactor;
        }

        return Math.min(10.0, Math.max(1.0, finalScore));
    }

    /**
     * Aggregate COST - Additive (total cost of all components)
     */
    private double aggregateCost(List<com.systemsimulator.model.Component> components) {
        if (components.isEmpty()) {
            return 10.0; // Perfect score (no cost)
        }

        double totalCostScore = 0.0;
        int count = 0;

        for (com.systemsimulator.model.Component component : components) {
            Double score = component.getHeuristics().getScore(Parameter.COST);
            if (score != null) {
                // Convert score to cost (10 - score gives actual cost impact)
                totalCostScore += (10.0 - score);
                count++;
            }
        }

        // More components = more cost
        double avgCost = count > 0 ? totalCostScore / count : 0;

        // Apply component count penalty
        double scaleFactor = Math.max(0.5, 1.0 - (components.size() * 0.02));
        double finalScore = Math.max(1.0, 10.0 - avgCost) * scaleFactor;

        return Math.min(10.0, Math.max(1.0, finalScore));
    }

    /**
     * Aggregate MINIMUM - Weakest link determines overall score
     * Used for AVAILABILITY, CONSISTENCY, SECURITY, DURABILITY
     * The architecture is only as strong as its weakest component
     */
    private double aggregateMinimum(Parameter param,
                                   List<com.systemsimulator.model.Component> components) {
        if (components.isEmpty()) {
            return 5.0; // Neutral score
        }

        double minimum = 10.0;

        for (com.systemsimulator.model.Component component : components) {
            Double score = component.getHeuristics().getScore(param);
            if (score != null && score < minimum) {
                minimum = score;
            }
        }

        return minimum;
    }

    /**
     * Aggregate AVERAGE - Average score across all components
     * Used for SCALABILITY, THROUGHPUT, MAINTAINABILITY, ENERGY_EFFICIENCY
     */
    private double aggregateAverage(Parameter param,
                                   List<com.systemsimulator.model.Component> components) {
        if (components.isEmpty()) {
            return 5.0; // Neutral score
        }

        double sum = 0.0;
        int count = 0;

        for (com.systemsimulator.model.Component component : components) {
            Double score = component.getHeuristics().getScore(param);
            if (score != null) {
                sum += score;
                count++;
            }
        }

        return count > 0 ? sum / count : 5.0;
    }

    /**
     * Calculate overall weighted score from parameter-wise aggregation
     */
    public double calculateOverallScore(Map<Parameter, Double> parameterScores,
                                       Map<Parameter, Double> weights) {
        if (parameterScores == null || parameterScores.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (Map.Entry<Parameter, Double> entry : parameterScores.entrySet()) {
            Parameter param = entry.getKey();
            double score = entry.getValue();
            double weight = weights.getOrDefault(param, 1.0);

            weightedSum += score * weight;
            totalWeight += weight;
        }

        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }

    /**
     * Main aggregation method with parameter-wise logic and weighted scoring
     */
    public double aggregate(List<com.systemsimulator.model.Component> components,
                          List<Link> links,
                          ParameterWeights weights) {
        if (components == null || components.isEmpty()) {
            return 0.0;
        }

        // Aggregate each parameter separately
        Map<Parameter, Double> parameterScores = aggregateByParameter(components, links);

        // Calculate weighted overall score
        double overallScore = calculateOverallScore(parameterScores, weights.getDefaultWeights());

        // Apply architectural penalties/bonuses
        double connectivityFactor = calculateConnectivityScore(components.size(),
                                                               links != null ? links.size() : 0);
        double complexityPenalty = calculateComplexityPenalty(components.size(),
                                                              links != null ? links.size() : 0);

        return overallScore * connectivityFactor * complexityPenalty;
    }

    /**
     * Get detailed parameter-wise scores for reporting
     */
    public Map<String, Object> getDetailedScores(List<com.systemsimulator.model.Component> components,
                                                  List<Link> links,
                                                  ParameterWeights weights) {
        Map<String, Object> result = new HashMap<>();

        // Parameter-wise scores
        Map<Parameter, Double> parameterScores = aggregateByParameter(components, links);
        result.put("parameterScores", parameterScores);

        // Overall weighted score
        double overallScore = calculateOverallScore(parameterScores, weights.getDefaultWeights());
        result.put("overallScore", overallScore);

        // Architecture metrics
        result.put("componentCount", components.size());
        result.put("linkCount", links != null ? links.size() : 0);
        result.put("connectivityScore", calculateConnectivityScore(components.size(),
                                                                    links != null ? links.size() : 0));
        result.put("complexityPenalty", calculateComplexityPenalty(components.size(),
                                                                    links != null ? links.size() : 0));

        // Bottleneck analysis
        if (components.size() > 0 && links != null && !links.isEmpty()) {
            Map<String, Double> bottlenecks = new HashMap<>();
            for (com.systemsimulator.model.Component component : components) {
                double bottleneckScore = calculateBottleneckScore(component, links);
                if (bottleneckScore < 1.0) {
                    bottlenecks.put(component.getName(), bottleneckScore);
                }
            }
            result.put("bottlenecks", bottlenecks);
        }

        return result;
    }

    /**
     * Calculate link factor based on architecture complexity
     * Well-connected systems get a bonus, poorly connected or overly complex get penalties
     */
    private double calculateLinkFactor(int componentCount, int linkCount) {
        if (componentCount == 0) {
            return 1.0;
        }

        if (linkCount == 0 && componentCount > 1) {
            return 0.5; // Penalty for disconnected architecture
        }

        // Ideal ratio: 1.5 to 3 links per component
        double ratio = (double) linkCount / componentCount;

        if (ratio < 1.0) {
            // Under-connected: slight penalty
            return 0.8 + (ratio * 0.2);
        } else if (ratio <= 3.0) {
            // Optimal range: full score
            return 1.0;
        } else if (ratio <= 5.0) {
            // Slightly over-connected: minor penalty
            return 1.0 - ((ratio - 3.0) * 0.05);
        } else {
            // Overly complex: significant penalty
            return Math.max(0.7, 1.0 - ((ratio - 3.0) * 0.08));
        }
    }

    /**
     * Calculate bottleneck score - identifies components with too many connections
     */
    public double calculateBottleneckScore(com.systemsimulator.model.Component component,
                                          List<Link> allLinks) {
        if (allLinks == null || allLinks.isEmpty()) {
            return 1.0; // No bottleneck if no links
        }

        int incomingLinks = 0;
        int outgoingLinks = 0;

        for (Link link : allLinks) {
            if (link.getTarget() != null && link.getTarget().getId().equals(component.getId())) {
                incomingLinks++;
            }
            if (link.getSource() != null && link.getSource().getId().equals(component.getId())) {
                outgoingLinks++;
            }
        }

        int totalConnections = incomingLinks + outgoingLinks;

        // Components with more than 10 connections are likely bottlenecks
        if (totalConnections > 10) {
            return 0.5; // High bottleneck risk
        } else if (totalConnections > 5) {
            return 0.7; // Medium bottleneck risk
        } else {
            return 1.0; // Low bottleneck risk
        }
    }

    /**
     * Evaluate parameter-specific scores across architecture
     */
    public Map<Parameter, Double> aggregateByParameter(List<com.systemsimulator.model.Component> components) {
        Map<Parameter, Double> aggregated = new HashMap<>();

        if (components == null || components.isEmpty()) {
            return aggregated;
        }

        for (Parameter param : Parameter.values()) {
            double sum = 0.0;
            int count = 0;

            for (com.systemsimulator.model.Component component : components) {
                Double score = component.getHeuristics().getScore(param);
                if (score != null) {
                    sum += score;
                    count++;
                }
            }

            aggregated.put(param, count > 0 ? sum / count : 0.0);
        }

        return aggregated;
    }

    /**
     * Calculate connectivity score - how well connected the architecture is
     */
    public double calculateConnectivityScore(int componentCount, int linkCount) {
        if (componentCount == 0) {
            return 0.0;
        }

        if (componentCount == 1) {
            return 1.0; // Single component is always "connected"
        }

        // Minimum links needed for connectivity: n-1 (spanning tree)
        int minLinks = componentCount - 1;

        if (linkCount < minLinks) {
            // Disconnected graph
            return (double) linkCount / minLinks * 0.5;
        }

        // Optimal connectivity: 1.5 to 3 links per component
        double ratio = (double) linkCount / componentCount;

        if (ratio >= 1.5 && ratio <= 3.0) {
            return 1.0;
        } else if (ratio < 1.5) {
            return 0.5 + (ratio / 1.5) * 0.5;
        } else {
            // Over-connected
            return Math.max(0.6, 1.0 - ((ratio - 3.0) * 0.1));
        }
    }

    /**
     * Calculate complexity penalty based on architecture size
     */
    public double calculateComplexityPenalty(int componentCount, int linkCount) {
        // Small architectures (< 5 components) - no penalty
        if (componentCount < 5) {
            return 1.0;
        }

        // Medium architectures (5-10 components) - slight penalty
        if (componentCount <= 10) {
            return 0.95;
        }

        // Large architectures (11-20 components) - moderate penalty
        if (componentCount <= 20) {
            return 0.90;
        }

        // Very large architectures (20+ components) - significant penalty
        return Math.max(0.75, 1.0 - (componentCount - 20) * 0.01);
    }

    /**
     * Get weighted average with custom weights map
     */
    public double getWeightedAverage(Map<Parameter, Double> scores, Map<Parameter, Double> weights) {
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (Map.Entry<Parameter, Double> entry : scores.entrySet()) {
            Parameter param = entry.getKey();
            double score = entry.getValue();
            double weight = weights.getOrDefault(param, 1.0);

            weightedSum += score * weight;
            totalWeight += weight;
        }

        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }
}
