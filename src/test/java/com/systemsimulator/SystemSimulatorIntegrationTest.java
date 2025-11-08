package com.systemsimulator;

import com.systemsimulator.model.*;
import com.systemsimulator.service.*;
import com.systemsimulator.utils.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SystemSimulatorIntegrationTest {

    @Autowired
    private ComponentService componentService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private ArchitectureService architectureService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private HeuristicService heuristicService;

    @Test
    void testCreateComponent() {
        // Create a database component
        Component database = componentService.createComponent(
                ComponentType.DATABASE,
                "db1",
                "PostgreSQL",
                Map.of("replicas", 2, "storageGB", 500)
        );

        assertNotNull(database);
        assertEquals("db1", database.getId());
        assertEquals("PostgreSQL", database.getName());
        assertEquals(ComponentType.DATABASE, database.getType());
        assertNotNull(database.getHeuristics());
    }

    @Test
    void testCreateMultipleComponents() {
        // Create API Service
        Component apiService = componentService.createComponent(
                ComponentType.API_SERVICE,
                "api1",
                "User Service",
                Map.of("instances", 3)
        );

        // Create Cache
        Component cache = componentService.createComponent(
                ComponentType.CACHE,
                "cache1",
                "Redis Cache",
                Map.of("memoryGB", 16)
        );

        // Create Database
        Component database = componentService.createComponent(
                ComponentType.DATABASE,
                "db1",
                "PostgreSQL",
                Map.of("replicas", 2)
        );

        List<Component> allComponents = componentService.getAllComponents();
        assertTrue(allComponents.size() >= 3);
    }

    @Test
    void testValidLinkCreation() {
        // Create components
        Component apiService = componentService.createComponent(
                ComponentType.API_SERVICE,
                "api-test-1",
                "API Server",
                Map.of()
        );

        Component database = componentService.createComponent(
                ComponentType.DATABASE,
                "db-test-1",
                "Database",
                Map.of()
        );

        // Validate link before creating
        boolean isValid = linkService.validateLink("api-test-1", "db-test-1", LinkType.API_CALL);
        assertTrue(isValid, "API Service should be able to call Database");

        // Create link
        Link link = linkService.createLink("link1", "api-test-1", "db-test-1", LinkType.API_CALL);
        assertNotNull(link);
        assertEquals(LinkType.API_CALL, link.getType());
    }

    @Test
    void testInvalidLinkCreation() {
        // Create components
        Component cache = componentService.createComponent(
                ComponentType.CACHE,
                "cache-test-1",
                "Cache",
                Map.of()
        );

        Component apiService = componentService.createComponent(
                ComponentType.API_SERVICE,
                "api-test-2",
                "API",
                Map.of()
        );

        // Cache to API is not valid via API_CALL
        boolean isValid = linkService.validateLink("cache-test-1", "api-test-2", LinkType.API_CALL);
        assertFalse(isValid, "Cache should not be able to call API Service via API_CALL");

        // Attempting to create should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            linkService.createLink("link-invalid", "cache-test-1", "api-test-2", LinkType.API_CALL);
        });
    }

    @Test
    void testArchitectureCreationAndEvaluation() {
        // Create architecture
        Architecture architecture = architectureService.createArchitecture("Test E-commerce System");
        assertNotNull(architecture);
        assertNotNull(architecture.getId());
        assertEquals("Test E-commerce System", architecture.getName());

        // Create components
        Component loadBalancer = componentService.createComponent(
                ComponentType.LOAD_BALANCER,
                "lb-test",
                "Load Balancer",
                Map.of()
        );

        Component apiService = componentService.createComponent(
                ComponentType.API_SERVICE,
                "api-test-arch",
                "API Service",
                Map.of("instances", 3)
        );

        Component cache = componentService.createComponent(
                ComponentType.CACHE,
                "cache-test-arch",
                "Redis",
                Map.of("memoryGB", 16)
        );

        Component database = componentService.createComponent(
                ComponentType.DATABASE,
                "db-test-arch",
                "PostgreSQL",
                Map.of("replicas", 2)
        );

        // Add components to architecture
        architecture.addComponent(loadBalancer);
        architecture.addComponent(apiService);
        architecture.addComponent(cache);
        architecture.addComponent(database);

        // Create links
        Link link1 = linkService.createLink("link-lb-api", "lb-test", "api-test-arch", LinkType.API_CALL);
        Link link2 = linkService.createLink("link-api-cache", "api-test-arch", "cache-test-arch", LinkType.CACHE_LOOKUP);
        Link link3 = linkService.createLink("link-api-db", "api-test-arch", "db-test-arch", LinkType.DATABASE_QUERY);

        architecture.addLink(link1);
        architecture.addLink(link2);
        architecture.addLink(link3);

        // Save architecture
        Architecture saved = architectureService.saveArchitecture(architecture);
        assertNotNull(saved);

        // Evaluate architecture
        ArchitectureService.ArchitectureEvaluation evaluation =
                architectureService.evaluateArchitectureDetailed(saved.getId());

        assertNotNull(evaluation);
        assertTrue(evaluation.getOverallScore() > 0, "Architecture should have a positive score");
        assertEquals(4, evaluation.getComponentCount());
        assertEquals(3, evaluation.getLinkCount());
        assertTrue(evaluation.isValid(), "Architecture should be valid");
        assertNotNull(evaluation.getInsights());
        assertFalse(evaluation.getInsights().isEmpty(), "Should have insights");
    }

    @Test
    void testHeuristicScoring() {
        // Test Database heuristics
        HeuristicProfile dbProfile = heuristicService.getDefaultHeuristicsForType(ComponentType.DATABASE);
        assertNotNull(dbProfile);

        Double durability = dbProfile.getScore(Parameter.DURABILITY);
        assertNotNull(durability);
        assertTrue(durability >= 9.0, "Database should have high durability score");

        // Test Cache heuristics
        HeuristicProfile cacheProfile = heuristicService.getDefaultHeuristicsForType(ComponentType.CACHE);
        Double latency = cacheProfile.getScore(Parameter.LATENCY);
        assertNotNull(latency);
        assertTrue(latency >= 9.0, "Cache should have very high latency score (low latency)");

        Double cacheDurability = cacheProfile.getScore(Parameter.DURABILITY);
        assertTrue(cacheDurability < 5.0, "Cache should have low durability score (volatile)");
    }

    @Test
    void testConnectionRuleValidation() {
        // Test API_CALL rules
        Component api = componentService.createComponent(
                ComponentType.API_SERVICE, "api-rule-test", "API", Map.of()
        );
        Component db = componentService.createComponent(
                ComponentType.DATABASE, "db-rule-test", "DB", Map.of()
        );
        Component cache = componentService.createComponent(
                ComponentType.CACHE, "cache-rule-test", "Cache", Map.of()
        );
        Component queue = componentService.createComponent(
                ComponentType.QUEUE, "queue-rule-test", "Queue", Map.of()
        );

        // Valid connections
        assertTrue(ruleEngineService.validateConnection(api, db, LinkType.API_CALL));
        assertTrue(ruleEngineService.validateConnection(api, cache, LinkType.CACHE_LOOKUP));
        assertTrue(ruleEngineService.validateConnection(api, queue, LinkType.EVENT_FLOW));

        // Test REPLICATION rules
        Component db2 = componentService.createComponent(
                ComponentType.DATABASE, "db-replica-test", "DB Replica", Map.of()
        );
        assertTrue(ruleEngineService.validateConnection(db, db2, LinkType.REPLICATION));

        // Invalid replication between different types
        assertFalse(ruleEngineService.validateConnection(db, cache, LinkType.REPLICATION));
    }

    @Test
    void testConnectionSuggestions() {
        Component api = componentService.createComponent(
                ComponentType.API_SERVICE, "api-suggest-test", "API", Map.of()
        );
        Component db = componentService.createComponent(
                ComponentType.DATABASE, "db-suggest-test", "DB", Map.of()
        );

        RuleEngineService.ConnectionSuggestion suggestion =
                ruleEngineService.getSuggestions(api, db);

        assertTrue(suggestion.isCanConnect());
        assertFalse(suggestion.getValidLinkTypes().isEmpty());
        assertTrue(suggestion.getValidLinkTypes().contains(LinkType.API_CALL) ||
                   suggestion.getValidLinkTypes().contains(LinkType.DATABASE_QUERY));
    }

    @Test
    void testBottleneckDetection() {
        // Create architecture with potential bottleneck
        Architecture arch = architectureService.createArchitecture("Bottleneck Test");

        Component central = componentService.createComponent(
                ComponentType.DATABASE, "central-db", "Central DB", Map.of()
        );
        arch.addComponent(central);

        // Create multiple components connecting to central database
        for (int i = 1; i <= 7; i++) {
            Component api = componentService.createComponent(
                    ComponentType.API_SERVICE, "api-" + i, "API " + i, Map.of()
            );
            arch.addComponent(api);

            Link link = linkService.createLink("link-" + i, "api-" + i, "central-db", LinkType.DATABASE_QUERY);
            arch.addLink(link);
        }

        architectureService.saveArchitecture(arch);

        ArchitectureService.ArchitectureEvaluation evaluation =
                architectureService.evaluateArchitectureDetailed(arch.getId());

        assertNotNull(evaluation.getBottlenecks());
        assertTrue(evaluation.getBottlenecks().size() > 0, "Should detect bottleneck in central database");
    }

    @Test
    void testArchitectureComparison() {
        // Create two architectures
        Architecture arch1 = architectureService.createArchitecture("Simple Architecture");
        Component api1 = componentService.createComponent(
                ComponentType.API_SERVICE, "api-comp-1", "API", Map.of()
        );
        Component db1 = componentService.createComponent(
                ComponentType.DATABASE, "db-comp-1", "DB", Map.of()
        );
        arch1.addComponent(api1);
        arch1.addComponent(db1);
        architectureService.saveArchitecture(arch1);

        Architecture arch2 = architectureService.createArchitecture("Optimized Architecture");
        Component lb2 = componentService.createComponent(
                ComponentType.LOAD_BALANCER, "lb-comp-2", "LB", Map.of()
        );
        Component api2 = componentService.createComponent(
                ComponentType.API_SERVICE, "api-comp-2", "API", Map.of("instances", 3)
        );
        Component cache2 = componentService.createComponent(
                ComponentType.CACHE, "cache-comp-2", "Cache", Map.of()
        );
        Component db2 = componentService.createComponent(
                ComponentType.DATABASE, "db-comp-2", "DB", Map.of("replicas", 2)
        );
        arch2.addComponent(lb2);
        arch2.addComponent(api2);
        arch2.addComponent(cache2);
        arch2.addComponent(db2);
        architectureService.saveArchitecture(arch2);

        // Compare architectures
        ArchitectureService.ArchitectureComparison comparison =
                architectureService.compareArchitectures(arch1.getId(), arch2.getId());

        assertNotNull(comparison);
        assertNotNull(comparison.getWinner());
        assertTrue(comparison.getArch2Score() > comparison.getArch1Score(),
                   "Optimized architecture should score higher");
    }

    @Test
    void testGetAllRules() {
        List<ConnectionRule> rules = ruleEngineService.getAllRules();
        assertNotNull(rules);
        assertEquals(8, rules.size(), "Should have 8 registered connection rules");
    }

    @Test
    void testComponentDeletion() {
        Component component = componentService.createComponent(
                ComponentType.API_SERVICE, "api-delete-test", "API to Delete", Map.of()
        );

        assertTrue(componentService.componentExists("api-delete-test"));

        componentService.deleteComponent("api-delete-test");

        assertFalse(componentService.componentExists("api-delete-test"));
    }
}

