package com.systemsimulator.controller;

import com.systemsimulator.model.*;
import com.systemsimulator.service.ComponentService;
import com.systemsimulator.service.HeuristicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/components")
@CrossOrigin(origins = "*")
public class ComponentController {

    private static final Logger logger = LoggerFactory.getLogger(ComponentController.class);

    @Autowired
    private ComponentService componentService;

    @Autowired
    private HeuristicService heuristicService;

    /**
     * Get all components
     */
    @GetMapping
    public ResponseEntity<List<Component>> getAllComponents() {
        return ResponseEntity.ok(componentService.getAllComponents());
    }

    /**
     * Get heuristics for a specific type+subtype (used by frontend preview)
     */
    @GetMapping("/heuristics/{type}/{subtype}")
    public ResponseEntity<HeuristicProfile> getHeuristicsForTypeAndSubtype(@PathVariable ComponentType type, @PathVariable String subtype) {
        HeuristicProfile heuristics = heuristicService.getHeuristicsForTypeAndSubtype(type, subtype);
        return ResponseEntity.ok(heuristics);
    }

    /**
     * Get component by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getComponentById(@PathVariable String id) {
        return componentService.getComponentById(id)
                .map(component -> ResponseEntity.ok((Object) component))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Component not found: " + id)));
    }

    /**
     * Create a new component
     */
    @PostMapping
    public ResponseEntity<?> createComponent(@RequestBody ComponentRequest request) {
        try {
            logger.info("Creating component: type={}, name={}", request.getType(), request.getName());

            String id = UUID.randomUUID().toString();
            Component component = componentService.createComponent(
                    request.getType(),
                    id,
                    request.getName(),
                    request.getProperties() != null ? request.getProperties() : Map.of()
            );

            logger.info("Successfully created component: id={}, type={}, name={}", id, request.getType(), request.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(component);
        } catch (Exception e) {
            logger.error("Failed to create component: type={}, name={}, error={}",
                    request.getType(), request.getName(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Failed to create component: " + e.getMessage()));
        }
    }

    /**
     * Update an existing component
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComponent(@PathVariable String id, @RequestBody Component component) {
        if (!componentService.componentExists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Component not found: " + id));
        }
        component.setId(id);
        Component updated = componentService.saveComponent(component);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a component
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComponent(@PathVariable String id) {
        if (!componentService.componentExists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Component not found: " + id));
        }
        componentService.deleteComponent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get component types
     */
    @GetMapping("/types")
    public ResponseEntity<ComponentType[]> getComponentTypes() {
        return ResponseEntity.ok(ComponentType.values());
    }

    /**
     * Get component count
     */
    @GetMapping("/count")
    public ResponseEntity<CountResponse> getComponentCount() {
        int count = componentService.getAllComponents().size();
        return ResponseEntity.ok(new CountResponse(count));
    }

    /**
     * Get components by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Component>> getComponentsByType(@PathVariable ComponentType type) {
        List<Component> components = componentService.getAllComponents().stream()
                .filter(c -> c.getType() == type)
                .toList();
        return ResponseEntity.ok(components);
    }

    /**
     * Check if component exists
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<ExistsResponse> checkComponentExists(@PathVariable String id) {
        boolean exists = componentService.componentExists(id);
        return ResponseEntity.ok(new ExistsResponse(id, exists));
    }

    /**
     * Get available subtypes for a component type
     */
    @GetMapping("/subtypes/{type}")
    public ResponseEntity<?> getSubtypesForType(@PathVariable ComponentType type) {
        List<String> subtypes = new java.util.ArrayList<>();

        switch (type) {
            case DATABASE:
                subtypes = java.util.Arrays.stream(DatabaseComponent.DatabaseType.values())
                        .map(Enum::name)
                        .toList();
                break;
            case CACHE:
                subtypes = java.util.Arrays.stream(CacheComponent.CacheType.values())
                        .map(Enum::name)
                        .toList();
                break;
            case API_SERVICE:
                subtypes = java.util.Arrays.stream(APIServiceComponent.APIType.values())
                        .map(Enum::name)
                        .toList();
                break;
            case QUEUE:
                subtypes = java.util.Arrays.stream(QueueComponent.QueueType.values())
                        .map(Enum::name)
                        .toList();
                break;
            case STORAGE:
                subtypes = java.util.Arrays.stream(StorageComponent.StorageType.values())
                        .map(Enum::name)
                        .toList();
                break;
            case LOAD_BALANCER:
                subtypes = java.util.Arrays.stream(LoadBalancerComponent.LoadBalancerType.values())
                        .map(Enum::name)
                        .toList();
                break;
            default:
                subtypes = List.of("default");
        }

        return ResponseEntity.ok(new SubtypesResponse(type, subtypes));
    }

    // ==================== DTOs ====================

    public static class ComponentRequest {
        private ComponentType type;
        private String name;
        private Map<String, Object> properties;

        public ComponentType getType() { return type; }
        public void setType(ComponentType type) { this.type = type; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }

    public static class ErrorResponse {
        private String error;
        private long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() { return error; }
        public long getTimestamp() { return timestamp; }
    }

    public static class CountResponse {
        private int count;

        public CountResponse(int count) {
            this.count = count;
        }

        public int getCount() { return count; }
    }

    public static class ExistsResponse {
        private String id;
        private boolean exists;

        public ExistsResponse(String id, boolean exists) {
            this.id = id;
            this.exists = exists;
        }

        public String getId() { return id; }
        public boolean isExists() { return exists; }
    }

    public static class SubtypesResponse {
        private ComponentType type;
        private List<String> subtypes;

        public SubtypesResponse(ComponentType type, List<String> subtypes) {
            this.type = type;
            this.subtypes = subtypes;
        }

        public ComponentType getType() { return type; }
        public List<String> getSubtypes() { return subtypes; }
    }
}
