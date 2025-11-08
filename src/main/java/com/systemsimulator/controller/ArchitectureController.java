package com.systemsimulator.controller;

import com.systemsimulator.model.*;
import com.systemsimulator.service.ArchitectureService;
import com.systemsimulator.service.RuleEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/architecture")
@CrossOrigin(origins = "*")
public class ArchitectureController {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureController.class);

    @Autowired
    private ArchitectureService architectureService;

    @Autowired
    private RuleEngineService ruleEngineService;

    /**
     * Health check endpoint to verify MongoDB connection
     */
    @GetMapping("/health/mongodb")
    public ResponseEntity<?> checkMongoDBHealth() {
        logger.info("MongoDB health check requested");
        boolean connected = architectureService.isMongoDBConnected();

        if (connected) {
            architectureService.logMongoDBStats();
            return ResponseEntity.ok(new HealthResponse(
                true,
                "MongoDB connection is healthy",
                architectureService.getAllArchitectures().size()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new HealthResponse(
                    false,
                    "MongoDB connection failed",
                    0
                ));
        }
    }

    /**
     * Get all architectures
     */
    @GetMapping
    public ResponseEntity<List<Architecture>> getAllArchitectures() {
        return ResponseEntity.ok(architectureService.getAllArchitectures());
    }

    /**
     * Get architecture by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Architecture> getArchitectureById(@PathVariable String id) {
        return architectureService.getArchitectureById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new architecture
     */
    @PostMapping
    public ResponseEntity<Architecture> createArchitecture(@RequestBody ArchitectureRequest request) {
        Architecture architecture = architectureService.createArchitecture(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(architecture);
    }

    /**
     * Add component to architecture
     */
    @PostMapping("/{id}/components")
    public ResponseEntity<?> addComponentToArchitecture(
            @PathVariable String id,
            @RequestBody ComponentReference componentRef) {
        try {
            Architecture updated = architectureService.addComponentToArchitectureById(id, componentRef.getComponentId());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Add link to architecture
     */
    @PostMapping("/{id}/links")
    public ResponseEntity<?> addLinkToArchitecture(
            @PathVariable String id,
            @RequestBody LinkReference linkRef) {
        try {
            Architecture updated = architectureService.addLinkToArchitectureById(id, linkRef.getLinkId());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Evaluate architecture (detailed)
     */
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateArchitecture(@RequestBody EvaluationRequest request) {
        try {
            ArchitectureService.ArchitectureEvaluation evaluation =
                    architectureService.evaluateArchitectureDetailed(request.getArchitectureId());
            return ResponseEntity.ok(evaluation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get simple score for architecture
     */
    @GetMapping("/{id}/score")
    public ResponseEntity<?> getArchitectureScore(@PathVariable String id) {
        try {
            double score = architectureService.evaluateArchitecture(id);
            return ResponseEntity.ok(new ScoreResponse(id, score));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Visualize architecture (get graph data)
     */
    @GetMapping("/visualize/{id}")
    public ResponseEntity<?> visualizeArchitecture(@PathVariable String id) {
        return architectureService.getArchitectureById(id)
                .map(arch -> {
                    VisualizationData data = new VisualizationData();
                    data.setArchitectureId(arch.getId());
                    data.setArchitectureName(arch.getName());
                    data.setComponents(arch.getComponents());
                    data.setLinks(arch.getLinks());
                    return ResponseEntity.ok((Object) data);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Architecture not found: " + id)));
    }


    /**
     * Validate architecture
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validateArchitecture(@PathVariable String id) {
        return architectureService.getArchitectureById(id)
                .map(arch -> {
                    RuleEngineService.ArchitectureValidationResult validation =
                            ruleEngineService.validateArchitecture(arch);
                    return ResponseEntity.ok((Object) validation);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Architecture not found: " + id)));
    }

    /**
     * Submit architecture to MongoDB
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitArchitecture(
            @PathVariable String id,
            @RequestBody SubmitRequest request) {
        logger.info("---------- SUBMIT REQUEST RECEIVED ----------");
        logger.info("Endpoint: POST /api/architecture/{}/submit", id);
        logger.info("Request Body - User ID: {}, Question ID: {}", request.getUserId(), request.getQuestionId());

        try {
            Architecture submitted = architectureService.submitArchitecture(
                    id,
                    request.getUserId(),
                    request.getQuestionId()
            );

            logger.info("✓ Submit endpoint completed successfully");
            logger.info("Returning architecture with ID: {}", submitted.getId());
            return ResponseEntity.ok(submitted);

        } catch (IllegalArgumentException e) {
            logger.error("✗ Submit endpoint failed - Architecture not found: {}", id);
            logger.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("✗ Submit endpoint failed with unexpected error");
            logger.error("Error type: {}", e.getClass().getName());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Stack trace:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to submit architecture: " + e.getMessage()));
        }
    }

    /**
     * Get architectures by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Architecture>> getArchitecturesByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(architectureService.getArchitecturesByUserId(userId));
    }

    /**
     * Get architectures by question ID
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<Architecture>> getArchitecturesByQuestionId(@PathVariable Integer questionId) {
        return ResponseEntity.ok(architectureService.getArchitecturesByQuestionId(questionId));
    }

    /**
     * Get all submitted architectures
     */
    @GetMapping("/submitted")
    public ResponseEntity<List<Architecture>> getSubmittedArchitectures() {
        return ResponseEntity.ok(architectureService.getSubmittedArchitectures());
    }

    /**
     * Delete architecture
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArchitecture(@PathVariable String id) {
        if (!architectureService.getArchitectureById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        architectureService.deleteArchitecture(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all connection rules
     */
    @GetMapping("/rules")
    public ResponseEntity<List<ConnectionRule>> getAllRules() {
        return ResponseEntity.ok(ruleEngineService.getAllRules());
    }

    /**
     * Get rules for specific link type
     */
    @GetMapping("/rules/{linkType}")
    public ResponseEntity<List<ConnectionRule>> getRulesForLinkType(@PathVariable LinkType linkType) {
        return ResponseEntity.ok(ruleEngineService.getRulesForLinkType(linkType));
    }

    // ==================== DTOs ====================

    public static class ArchitectureRequest {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class EvaluationRequest {
        private String architectureId;

        public String getArchitectureId() { return architectureId; }
        public void setArchitectureId(String architectureId) { this.architectureId = architectureId; }
    }

    public static class SubmitRequest {
        private Integer userId;
        private Integer questionId;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public Integer getQuestionId() { return questionId; }
        public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    }

    public static class ComparisonRequest {
        private String architecture1Id;
        private String architecture2Id;

        public String getArchitecture1Id() { return architecture1Id; }
        public void setArchitecture1Id(String architecture1Id) { this.architecture1Id = architecture1Id; }

        public String getArchitecture2Id() { return architecture2Id; }
        public void setArchitecture2Id(String architecture2Id) { this.architecture2Id = architecture2Id; }
    }

    public static class ScoreResponse {
        private String architectureId;
        private double score;

        public ScoreResponse(String architectureId, double score) {
            this.architectureId = architectureId;
            this.score = score;
        }

        public String getArchitectureId() { return architectureId; }
        public double getScore() { return score; }
    }

    public static class VisualizationData {
        private String architectureId;
        private String architectureName;
        private List<Component> components;
        private List<Link> links;

        public String getArchitectureId() { return architectureId; }
        public void setArchitectureId(String architectureId) { this.architectureId = architectureId; }

        public String getArchitectureName() { return architectureName; }
        public void setArchitectureName(String architectureName) { this.architectureName = architectureName; }

        public List<Component> getComponents() { return components; }
        public void setComponents(List<Component> components) { this.components = components; }

        public List<Link> getLinks() { return links; }
        public void setLinks(List<Link> links) { this.links = links; }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }

    public static class ComponentReference {
        private String componentId;

        public String getComponentId() { return componentId; }
        public void setComponentId(String componentId) { this.componentId = componentId; }
    }

    public static class LinkReference {
        private String linkId;

        public String getLinkId() { return linkId; }
        public void setLinkId(String linkId) { this.linkId = linkId; }
    }

    public static class HealthResponse {
        private boolean connected;
        private String message;
        private long totalArchitectures;

        public HealthResponse(boolean connected, String message, long totalArchitectures) {
            this.connected = connected;
            this.message = message;
            this.totalArchitectures = totalArchitectures;
        }

        public boolean isConnected() { return connected; }
        public String getMessage() { return message; }
        public long getTotalArchitectures() { return totalArchitectures; }
    }
}
