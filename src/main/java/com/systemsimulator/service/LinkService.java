package com.systemsimulator.service;

import com.systemsimulator.model.*;
import com.systemsimulator.repository.InMemoryLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LinkService {

    @Autowired
    private InMemoryLinkRepository linkRepository;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private HeuristicService heuristicService;

    /**
     * Create a new link between components with validation
     */
    public Link createLink(String id, String sourceId, String targetId, LinkType linkType) {
        Component source = componentService.getComponentById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source component not found: " + sourceId));
        Component target = componentService.getComponentById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target component not found: " + targetId));

        // Validate the link using rule engine
        if (!ruleEngineService.validateConnection(source, target, linkType)) {
            throw new IllegalArgumentException(
                String.format("Invalid connection: %s (%s) -> %s (%s) via %s. Check connection rules.",
                    source.getName(), source.getType(),
                    target.getName(), target.getType(),
                    linkType)
            );
        }

        Link link = new Link(id, source, target, linkType);

        // Initialize default heuristics for the link type
        HeuristicProfile heuristics = heuristicService.getDefaultHeuristicsForLinkType(linkType);
        link.setHeuristics(heuristics);

        return linkRepository.save(link);
    }

    /**
     * Validate a potential link without creating it
     */
    public boolean validateLink(String sourceId, String targetId, LinkType linkType) {
        Optional<Component> source = componentService.getComponentById(sourceId);
        Optional<Component> target = componentService.getComponentById(targetId);

        if (!source.isPresent() || !target.isPresent()) {
            return false;
        }

        return ruleEngineService.validateConnection(source.get(), target.get(), linkType);
    }

    /**
     * Validate link and get detailed reason if invalid
     */
    public ValidationResult validateLinkWithReason(String sourceId, String targetId, LinkType linkType) {
        Optional<Component> sourceOpt = componentService.getComponentById(sourceId);
        Optional<Component> targetOpt = componentService.getComponentById(targetId);

        if (!sourceOpt.isPresent()) {
            return new ValidationResult(false, "Source component not found: " + sourceId);
        }

        if (!targetOpt.isPresent()) {
            return new ValidationResult(false, "Target component not found: " + targetId);
        }

        Component source = sourceOpt.get();
        Component target = targetOpt.get();

        boolean isValid = ruleEngineService.validateConnection(source, target, linkType);

        if (isValid) {
            return new ValidationResult(true, "Connection is valid");
        } else {
            String reason = String.format(
                "Connection not allowed: %s (%s) -> %s (%s) via %s. " +
                "Please check CONNECTION_RULES.md for valid connection patterns.",
                source.getName(), source.getType(),
                target.getName(), target.getType(),
                linkType
            );
            return new ValidationResult(false, reason);
        }
    }

    /**
     * Save or update a link
     */
    public Link saveLink(Link link) {
        return linkRepository.save(link);
    }

    /**
     * Get link by ID
     */
    public Optional<Link> getLinkById(String id) {
        return linkRepository.findById(id);
    }

    /**
     * Get all links
     */
    public List<Link> getAllLinks() {
        return linkRepository.findAll();
    }

    /**
     * Delete link by ID
     */
    public void deleteLink(String id) {
        linkRepository.deleteById(id);
    }

    /**
     * Get all links originating from a component
     */
    public List<Link> getLinksBySource(String sourceId) {
        return linkRepository.findBySourceId(sourceId);
    }

    /**
     * Get all links pointing to a component
     */
    public List<Link> getLinksByTarget(String targetId) {
        return linkRepository.findByTargetId(targetId);
    }

    /**
     * Get all links connected to a component (incoming + outgoing)
     */
    public List<Link> getLinksForComponent(String componentId) {
        List<Link> links = new java.util.ArrayList<>();
        links.addAll(getLinksBySource(componentId));
        links.addAll(getLinksByTarget(componentId));
        return links;
    }

    /**
     * Check if a component has any connections
     */
    public boolean isComponentConnected(String componentId) {
        return !getLinksBySource(componentId).isEmpty() ||
               !getLinksByTarget(componentId).isEmpty();
    }

    /**
     * Get connection count for a component
     */
    public ConnectionStats getConnectionStats(String componentId) {
        int incoming = getLinksByTarget(componentId).size();
        int outgoing = getLinksBySource(componentId).size();
        return new ConnectionStats(incoming, outgoing);
    }

    /**
     * Delete all links connected to a component (useful before deleting component)
     */
    public void deleteLinksForComponent(String componentId) {
        List<Link> links = getLinksForComponent(componentId);
        for (Link link : links) {
            deleteLink(link.getId());
        }
    }

    // Inner classes for responses
    public static class ValidationResult {
        private final boolean valid;
        private final String reason;

        public ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public boolean isValid() { return valid; }
        public String getReason() { return reason; }
    }

    public static class ConnectionStats {
        private final int incomingLinks;
        private final int outgoingLinks;

        public ConnectionStats(int incomingLinks, int outgoingLinks) {
            this.incomingLinks = incomingLinks;
            this.outgoingLinks = outgoingLinks;
        }

        public int getIncomingLinks() { return incomingLinks; }
        public int getOutgoingLinks() { return outgoingLinks; }
        public int getTotalConnections() { return incomingLinks + outgoingLinks; }
    }
}
