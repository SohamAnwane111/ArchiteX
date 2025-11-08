package com.systemsimulator.controller;

import com.systemsimulator.model.*;
import com.systemsimulator.service.LinkService;
import com.systemsimulator.service.RuleEngineService;
import com.systemsimulator.service.HeuristicService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.systemsimulator.service.ComponentService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/links")
@CrossOrigin(origins = "*")
public class LinkController {

    @Autowired
    private LinkService linkService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private HeuristicService heuristicService;

    /**
     * Get all links
     */
    @GetMapping
    public ResponseEntity<List<Link>> getAllLinks() {
        return ResponseEntity.ok(linkService.getAllLinks());
    }

    /**
     * Get link by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLinkById(@PathVariable String id) {
        return linkService.getLinkById(id)
                .map(link -> ResponseEntity.ok((Object) link))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Link not found: " + id)));
    }

    /**
     * Validate a potential link (without creating it)
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateLink(@RequestBody LinkValidationRequest request) {
        LinkService.ValidationResult result = linkService.validateLinkWithReason(
                request.getSourceId(),
                request.getTargetId(),
                request.getLinkType()
        );

        ValidationResponse response = new ValidationResponse(
                result.isValid(),
                result.getReason()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get suggestions for connecting two components
     */
    @PostMapping("/suggest")
    public ResponseEntity<?> getSuggestions(@RequestBody SuggestionRequest request) {
        try {
            Optional<Component> sourceOpt = componentService.getComponentById(request.getSourceId());
            Optional<Component> targetOpt = componentService.getComponentById(request.getTargetId());

            if (sourceOpt.isEmpty() || targetOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Component not found"));
            }

            RuleEngineService.ConnectionSuggestion suggestion =
                    ruleEngineService.getSuggestions(sourceOpt.get(), targetOpt.get());

            return ResponseEntity.ok(suggestion);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Create a new link
     */
    @PostMapping
    public ResponseEntity<?> createLink(@RequestBody LinkRequest request) {
        try {
            String id = UUID.randomUUID().toString();
            Link link = linkService.createLink(
                    id,
                    request.getSourceId(),
                    request.getTargetId(),
                    request.getLinkType()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(link);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete a link
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLink(@PathVariable String id) {
        if (linkService.getLinkById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Link not found: " + id));
        }
        linkService.deleteLink(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all links for a component (incoming + outgoing)
     */
    @GetMapping("/component/{componentId}")
    public ResponseEntity<List<Link>> getLinksForComponent(@PathVariable String componentId) {
        List<Link> links = linkService.getLinksForComponent(componentId);
        return ResponseEntity.ok(links);
    }

    /**
     * Get links by source component
     */
    @GetMapping("/source/{sourceId}")
    public ResponseEntity<List<Link>> getLinksBySource(@PathVariable String sourceId) {
        return ResponseEntity.ok(linkService.getLinksBySource(sourceId));
    }

    /**
     * Get links by target component
     */
    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<Link>> getLinksByTarget(@PathVariable String targetId) {
        return ResponseEntity.ok(linkService.getLinksByTarget(targetId));
    }

    /**
     * Get connection statistics for a component
     */
    @GetMapping("/stats/{componentId}")
    public ResponseEntity<?> getConnectionStats(@PathVariable String componentId) {
        try {
            LinkService.ConnectionStats stats = linkService.getConnectionStats(componentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Component not found: " + componentId));
        }
    }

    /**
     * Check if a component is connected to any other component
     */
    @GetMapping("/component/{componentId}/connected")
    public ResponseEntity<ConnectedResponse> isComponentConnected(@PathVariable String componentId) {
        boolean connected = linkService.isComponentConnected(componentId);
        return ResponseEntity.ok(new ConnectedResponse(componentId, connected));
    }

    /**
     * Get all link types
     */
    @GetMapping("/types")
    public ResponseEntity<LinkType[]> getLinkTypes() {
        return ResponseEntity.ok(LinkType.values());
    }

    /**
     * Get link count
     */
    @GetMapping("/count")
    public ResponseEntity<CountResponse> getLinkCount() {
        int count = linkService.getAllLinks().size();
        return ResponseEntity.ok(new CountResponse(count));
    }

    /**
     * Delete all links for a component
     */
    @DeleteMapping("/component/{componentId}")
    public ResponseEntity<DeleteResponse> deleteLinksForComponent(@PathVariable String componentId) {
        List<Link> links = linkService.getLinksForComponent(componentId);
        int deletedCount = links.size();
        linkService.deleteLinksForComponent(componentId);
        return ResponseEntity.ok(new DeleteResponse(deletedCount, "Links deleted successfully"));
    }

    /**
     * Get heuristics for a specific link
     */
    @GetMapping("/{id}/heuristics")
    public ResponseEntity<?> getLinkHeuristics(@PathVariable String id) {
        Optional<Link> linkOpt = linkService.getLinkById(id);
        if (linkOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Link not found: " + id));
        }
        return ResponseEntity.ok(linkOpt.get().getHeuristics());
    }

    /**
     * Get default heuristics for a link type
     */
    @GetMapping("/heuristics/default/{linkType}")
    public ResponseEntity<HeuristicProfile> getDefaultHeuristicsForLinkType(@PathVariable LinkType linkType) {
        HeuristicProfile heuristics = heuristicService.getDefaultHeuristicsForLinkType(linkType);
        return ResponseEntity.ok(heuristics);
    }

    /**
     * Update heuristics for a specific link
     */
    @PutMapping("/{id}/heuristics")
    public ResponseEntity<?> updateLinkHeuristics(
            @PathVariable String id,
            @RequestBody HeuristicUpdateRequest request) {
        try {
            Optional<Link> linkOpt = linkService.getLinkById(id);
            if (linkOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Link not found: " + id));
            }

            Link link = linkOpt.get();
            heuristicService.updateLinkHeuristicScore(link, request.getParameter(), request.getScore());
            linkService.saveLink(link);

            return ResponseEntity.ok(link.getHeuristics());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Calculate weighted score for a link
     */
    @PostMapping("/{id}/heuristics/score")
    public ResponseEntity<?> calculateLinkScore(
            @PathVariable String id,
            @RequestBody java.util.Map<Parameter, Double> weights) {
        try {
            Optional<Link> linkOpt = linkService.getLinkById(id);
            if (linkOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Link not found: " + id));
            }

            double score = heuristicService.calculateLinkScore(linkOpt.get(), weights);
            return ResponseEntity.ok(new ScoreResponse(score));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==================== DTOs ====================

    @Setter
    @Getter
    public static class LinkRequest {
        private String sourceId;
        private String targetId;
        private LinkType linkType;

    }

    @Setter
    @Getter
    public static class LinkValidationRequest {
        private String sourceId;
        private String targetId;
        private LinkType linkType;

    }

    @Setter
    @Getter
    public static class SuggestionRequest {
        private String sourceId;
        private String targetId;

    }

    @Getter
    public static class ValidationResponse {
        private final boolean valid;
        private final String message;

        public ValidationResponse(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

    }

    @Getter
    public static class ConnectedResponse {
        private final String componentId;
        private final boolean connected;

        public ConnectedResponse(String componentId, boolean connected) {
            this.componentId = componentId;
            this.connected = connected;
        }
    }

    @Getter
    public static class CountResponse {
        private final int count;

        public CountResponse(int count) {
            this.count = count;
        }

    }

    @Getter
    public static class DeleteResponse {
        private final int deletedCount;
        private final String message;

        public DeleteResponse(int deletedCount, String message) {
            this.deletedCount = deletedCount;
            this.message = message;
        }

    }

    @Getter
    public static class ErrorResponse {
        private final String error;
        private final long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

    }

    @Setter
    @Getter
    public static class HeuristicUpdateRequest {
        private Parameter parameter;
        private double score;

    }

    @Getter
    public static class ScoreResponse {
        private final double score;

        public ScoreResponse(double score) {
            this.score = score;
        }

    }
}

