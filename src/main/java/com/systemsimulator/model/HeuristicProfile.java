package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class HeuristicProfile {
    private Map<Parameter, Double> scores = new HashMap<>();

    public HeuristicProfile() {}

    public HeuristicProfile(Map<Parameter, Double> scores) {
        this.scores = scores;
    }

    public void setScore(Parameter param, double value) {
        scores.put(param, value);
    }

    public Double getScore(Parameter param) {
        return scores.getOrDefault(param, 0.0);
    }

    public double getWeightedScore(Map<Parameter, Double> weights) {
        double total = 0.0;
        double weightSum = 0.0;
        for (Map.Entry<Parameter, Double> entry : scores.entrySet()) {
            double weight = weights.getOrDefault(entry.getKey(), 1.0);
            total += entry.getValue() * weight;
            weightSum += weight;
        }
        return weightSum > 0 ? total / weightSum : 0.0;
    }
}

