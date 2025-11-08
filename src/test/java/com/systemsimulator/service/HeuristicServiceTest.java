package com.systemsimulator.service;

import com.systemsimulator.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HeuristicServiceTest {

    @Autowired
    private HeuristicService heuristicService;

    @Test
    void testDatabaseHeuristics() {
        HeuristicProfile profile = heuristicService.getDefaultHeuristicsForType(ComponentType.DATABASE);

        assertNotNull(profile);

        // Database should have high durability
        assertTrue(profile.getScore(Parameter.DURABILITY) >= 9.0,
                   "Database should have high durability");

        // Database should have strong consistency
        assertTrue(profile.getScore(Parameter.CONSISTENCY) >= 8.0,
                   "Database should have strong consistency");

        // Database should have moderate latency (not the fastest)
        assertTrue(profile.getScore(Parameter.LATENCY) < 8.0,
                   "Database should have moderate latency");
    }

    @Test
    void testCacheHeuristics() {
        HeuristicProfile profile = heuristicService.getDefaultHeuristicsForType(ComponentType.CACHE);

        // Cache should have excellent latency
        assertTrue(profile.getScore(Parameter.LATENCY) >= 9.0,
                   "Cache should have excellent latency");

        // Cache should have low durability (volatile)
        assertTrue(profile.getScore(Parameter.DURABILITY) < 5.0,
                   "Cache should have low durability");

        // Cache should have high throughput
        assertTrue(profile.getScore(Parameter.THROUGHPUT) >= 9.0,
                   "Cache should have high throughput");
    }

    @Test
    void testLoadBalancerHeuristics() {
        HeuristicProfile profile = heuristicService.getDefaultHeuristicsForType(ComponentType.LOAD_BALANCER);

        // Load balancer should have high availability
        assertTrue(profile.getScore(Parameter.AVAILABILITY) >= 9.0,
                   "Load balancer should have high availability");

        // Load balancer should be low cost
        assertTrue(profile.getScore(Parameter.COST) <= 6.0,
                   "Load balancer should be cost-effective");
    }

    @Test
    void testAllComponentTypesHaveHeuristics() {
        for (ComponentType type : ComponentType.values()) {
            HeuristicProfile profile = heuristicService.getDefaultHeuristicsForType(type);
            assertNotNull(profile, "All component types should have heuristic profiles");
            assertFalse(profile.getScores().isEmpty(),
                       "Heuristic profile should have scores for " + type);
        }
    }

    @Test
    void testPropertyAdjustments() {
        Component component = new DatabaseComponent("db1", "DB", DatabaseComponent.DatabaseType.SQL);

        // Set initial heuristics
        HeuristicProfile baseProfile = heuristicService.getDefaultHeuristicsForType(ComponentType.DATABASE);
        component.setHeuristics(baseProfile);

        double baseAvailability = component.getHeuristics().getScore(Parameter.AVAILABILITY);

        // Apply property adjustments
        HeuristicProfile adjustedProfile = heuristicService.getAdjustedHeuristics(
            ComponentType.DATABASE,
            Map.of("replicas", 3)
        );

        // With replicas, availability should be higher
        assertTrue(adjustedProfile.getScore(Parameter.AVAILABILITY) >= baseAvailability,
                   "Replicas should improve availability");
    }

    @Test
    void testScoreBoundaries() {
        Component component = new CacheComponent("cache1", "Cache", CacheComponent.CacheType.IN_MEMORY);
        HeuristicProfile profile = new HeuristicProfile();

        // Test that scores stay within bounds
        heuristicService.updateHeuristicScore(component, Parameter.LATENCY, 9.5);
        assertEquals(9.5, component.getHeuristics().getScore(Parameter.LATENCY));

        // Test invalid scores
        assertThrows(IllegalArgumentException.class, () -> {
            heuristicService.updateHeuristicScore(component, Parameter.LATENCY, 11.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            heuristicService.updateHeuristicScore(component, Parameter.LATENCY, -1.0);
        });
    }
}

