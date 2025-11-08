package com.systemsimulator.utils;

import com.systemsimulator.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class HeuristicAggregatorTest {

    private HeuristicAggregator aggregator;
    private ParameterWeights weights;

    @BeforeEach
    void setUp() {
        aggregator = new HeuristicAggregator();
        weights = new ParameterWeights();
    }

    @Test
    void testAggregateWithEmptyComponents() {
        double score = aggregator.aggregate(new ArrayList<>(), new ArrayList<>(), weights);
        assertEquals(0.0, score, "Empty architecture should have 0 score");
    }

    @Test
    void testAggregateWithSingleComponent() {
        Component db = new DatabaseComponent("db1", "Database", DatabaseComponent.DatabaseType.SQL);
        HeuristicProfile profile = new HeuristicProfile();
        profile.setScore(Parameter.LATENCY, 8.0);
        profile.setScore(Parameter.DURABILITY, 9.0);
        profile.setScore(Parameter.AVAILABILITY, 8.5);
        db.setHeuristics(profile);

        List<Component> components = List.of(db);
        double score = aggregator.aggregate(components, new ArrayList<>(), weights);

        assertTrue(score > 0, "Single component should have positive score");
    }

    @Test
    void testBottleneckDetection() {
        Component db = new DatabaseComponent("db1", "Database", DatabaseComponent.DatabaseType.SQL);
        Component api1 = new APIServiceComponent("api1", "API1", APIServiceComponent.APIType.REST);
        Component api2 = new APIServiceComponent("api2", "API2", APIServiceComponent.APIType.REST);

        // Create many links to database (potential bottleneck)
        List<Link> links = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Component api = new APIServiceComponent("api" + i, "API" + i, APIServiceComponent.APIType.REST);
            Link link = new Link("link" + i, api, db, LinkType.DATABASE_QUERY);
            links.add(link);
        }

        double bottleneckScore = aggregator.calculateBottleneckScore(db, links);

        assertTrue(bottleneckScore < 0.8, "Database with 12 connections should be flagged as bottleneck");
        assertTrue(bottleneckScore <= 0.5, "Should have high bottleneck risk");
    }

    @Test
    void testLowBottleneckScore() {
        Component api = new APIServiceComponent("api1", "API", APIServiceComponent.APIType.REST);
        Component db = new DatabaseComponent("db1", "DB", DatabaseComponent.DatabaseType.SQL);

        List<Link> links = List.of(
            new Link("link1", api, db, LinkType.DATABASE_QUERY)
        );

        double bottleneckScore = aggregator.calculateBottleneckScore(db, links);

        assertEquals(1.0, bottleneckScore, "Component with few connections should have low bottleneck risk");
    }

    @Test
    void testAggregateByParameter() {
        Component db = new DatabaseComponent("db1", "DB", DatabaseComponent.DatabaseType.SQL);
        HeuristicProfile dbProfile = new HeuristicProfile();
        dbProfile.setScore(Parameter.LATENCY, 6.0);
        dbProfile.setScore(Parameter.DURABILITY, 9.5);
        db.setHeuristics(dbProfile);

        Component cache = new CacheComponent("cache1", "Cache", CacheComponent.CacheType.IN_MEMORY);
        HeuristicProfile cacheProfile = new HeuristicProfile();
        cacheProfile.setScore(Parameter.LATENCY, 9.5);
        cacheProfile.setScore(Parameter.DURABILITY, 3.0);
        cache.setHeuristics(cacheProfile);

        List<Component> components = List.of(db, cache);
        Map<Parameter, Double> aggregated = aggregator.aggregateByParameter(components);

        assertNotNull(aggregated);

        // Average latency should be (6.0 + 9.5) / 2 = 7.75
        assertEquals(7.75, aggregated.get(Parameter.LATENCY), 0.01);

        // Average durability should be (9.5 + 3.0) / 2 = 6.25
        assertEquals(6.25, aggregated.get(Parameter.DURABILITY), 0.01);
    }

    @Test
    void testConnectivityScore() {
        // Well-connected architecture (2 components, 2 links)
        double goodScore = aggregator.calculateConnectivityScore(2, 2);
        assertTrue(goodScore >= 0.8, "Well-connected architecture should score high");

        // Disconnected architecture (3 components, 1 link)
        double poorScore = aggregator.calculateConnectivityScore(3, 1);
        assertTrue(poorScore < 0.6, "Disconnected architecture should score low");

        // Over-connected architecture
        double overScore = aggregator.calculateConnectivityScore(3, 15);
        assertTrue(overScore < 1.0, "Over-connected architecture should have penalty");
    }

    @Test
    void testComplexityPenalty() {
        // Small architecture - no penalty
        assertEquals(1.0, aggregator.calculateComplexityPenalty(3, 5));

        // Medium architecture - slight penalty
        assertEquals(0.95, aggregator.calculateComplexityPenalty(8, 12));

        // Large architecture - moderate penalty
        assertEquals(0.90, aggregator.calculateComplexityPenalty(15, 25));

        // Very large architecture - higher penalty
        double veryLargePenalty = aggregator.calculateComplexityPenalty(25, 50);
        assertTrue(veryLargePenalty < 0.90);
    }

    @Test
    void testWeightedAverage() {
        Map<Parameter, Double> scores = new HashMap<>();
        scores.put(Parameter.LATENCY, 8.0);
        scores.put(Parameter.COST, 6.0);
        scores.put(Parameter.AVAILABILITY, 9.0);

        Map<Parameter, Double> customWeights = new HashMap<>();
        customWeights.put(Parameter.LATENCY, 2.0);  // High priority
        customWeights.put(Parameter.COST, 0.5);     // Low priority
        customWeights.put(Parameter.AVAILABILITY, 1.5); // Medium-high priority

        double weighted = aggregator.getWeightedAverage(scores, customWeights);

        // Calculation: (8.0*2.0 + 6.0*0.5 + 9.0*1.5) / (2.0+0.5+1.5) = 32.5 / 4.0 = 8.125
        assertEquals(8.125, weighted, 0.01);
    }
}

