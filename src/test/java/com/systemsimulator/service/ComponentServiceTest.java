package com.systemsimulator.service;

import com.systemsimulator.model.*;
import com.systemsimulator.repository.InMemoryComponentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ComponentServiceTest {

    @Mock
    private InMemoryComponentRepository componentRepository;

    @Mock
    private HeuristicService heuristicService;

    @InjectMocks
    private ComponentService componentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateDatabaseComponent() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();
        properties.put("replicas", 2);
        properties.put("storageGB", 500);

        HeuristicProfile mockProfile = new HeuristicProfile();
        mockProfile.setScore(Parameter.DURABILITY, 9.5);

        when(heuristicService.getDefaultHeuristicsForType(ComponentType.DATABASE))
                .thenReturn(mockProfile);
        when(componentRepository.save(any(Component.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Component result = componentService.createComponent(
                ComponentType.DATABASE,
                "db1",
                "PostgreSQL",
                properties
        );

        // Assert
        assertNotNull(result);
        assertEquals("db1", result.getId());
        assertEquals("PostgreSQL", result.getName());
        assertEquals(ComponentType.DATABASE, result.getType());
        verify(componentRepository, times(1)).save(any(Component.class));
        verify(heuristicService, times(1)).getDefaultHeuristicsForType(ComponentType.DATABASE);
    }

    @Test
    void testCreateCacheComponent() {
        // Arrange
        HeuristicProfile mockProfile = new HeuristicProfile();
        mockProfile.setScore(Parameter.LATENCY, 9.5);
        mockProfile.setScore(Parameter.DURABILITY, 3.0);

        when(heuristicService.getDefaultHeuristicsForType(ComponentType.CACHE))
                .thenReturn(mockProfile);
        when(componentRepository.save(any(Component.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Component result = componentService.createComponent(
                ComponentType.CACHE,
                "cache1",
                "Redis",
                Map.of("memoryGB", 16)
        );

        // Assert
        assertNotNull(result);
        assertEquals(ComponentType.CACHE, result.getType());
        assertTrue(result.getHeuristics().getScore(Parameter.LATENCY) >= 9.0);
    }

    @Test
    void testUnsupportedComponentType() {
        // This should throw exception for unsupported types
        when(heuristicService.getDefaultHeuristicsForType(any()))
                .thenReturn(new HeuristicProfile());

        assertThrows(IllegalArgumentException.class, () -> {
            // Using a trick to test - passing null would cause NPE
            componentService.createComponent(null, "test", "test", Map.of());
        });
    }
}

