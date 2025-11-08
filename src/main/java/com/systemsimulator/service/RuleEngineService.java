package com.systemsimulator.service;

import com.systemsimulator.model.*;
import com.systemsimulator.utils.ConnectionRuleRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class RuleEngineService {

    @Autowired
    private ConnectionRuleRegistry ruleRegistry;

    /**
     * Validate if a connection between two components is allowed
     */
    public boolean validateConnection(Component source, Component target, LinkType linkType) {
        if (source == null || target == null || linkType == null) {
            return false;
        }

        List<ConnectionRule> rules = ruleRegistry.getRulesForLinkType(linkType);

        if (rules.isEmpty()) {
            return false; // No rules defined for this link type means connection not allowed
        }

        // Check if any rule allows this connection
        for (ConnectionRule rule : rules) {
            if (rule.isValid(source, target, linkType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all registered connection rules
     */
    public List<ConnectionRule> getAllRules() {
        return ruleRegistry.getAllRules();
    }

    /**
     * Register a new connection rule dynamically
     */
    public void registerRule(ConnectionRule rule) {
        ruleRegistry.registerRule(rule);
    }

    /**
     * Get all rules for a specific link type
     */
    public List<ConnectionRule> getRulesForLinkType(LinkType linkType) {
        return ruleRegistry.getRulesForLinkType(linkType);
    }

    /**
     * Check if rules exist for a link type
     */
    public boolean hasRulesFor(LinkType linkType) {
        return ruleRegistry.hasRuleFor(linkType);
    }

    /**
     * Get all valid link types for a source-target component pair
     */
    public List<LinkType> getValidLinkTypes(Component source, Component target) {
        List<LinkType> validTypes = new ArrayList<>();

        for (LinkType linkType : LinkType.values()) {
            if (validateConnection(source, target, linkType)) {
                validTypes.add(linkType);
            }
        }

        return validTypes;
    }

    /**
     * Get suggestions for connecting two components
     */
    public ConnectionSuggestion getSuggestions(Component source, Component target) {
        List<LinkType> validTypes = getValidLinkTypes(source, target);

        if (validTypes.isEmpty()) {
            return new ConnectionSuggestion(
                false,
                validTypes,
                String.format("No valid connection types found between %s (%s) and %s (%s). " +
                             "These component types cannot be directly connected.",
                    source.getName(), source.getType(),
                    target.getName(), target.getType())
            );
        }

        return new ConnectionSuggestion(
            true,
            validTypes,
            String.format("You can connect %s (%s) to %s (%s) using: %s",
                source.getName(), source.getType(),
                target.getName(), target.getType(),
                String.join(", ", validTypes.stream().map(Enum::name).toArray(String[]::new)))
        );
    }

    /**
     * Validate an entire architecture for rule violations
     */
    public ArchitectureValidationResult validateArchitecture(Architecture architecture) {
        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (architecture == null) {
            violations.add("Architecture is null");
            return new ArchitectureValidationResult(false, violations, warnings);
        }

        // Check if architecture has components
        if (architecture.getComponents().isEmpty()) {
            warnings.add("Architecture has no components");
        }

        // Check if components are connected
        if (architecture.getComponents().size() > 1 && architecture.getLinks().isEmpty()) {
            warnings.add("Architecture has multiple components but no links");
        }

        // Validate each link
        for (Link link : architecture.getLinks()) {
            if (link.getSource() == null) {
                violations.add("Link " + link.getId() + " has null source");
                continue;
            }
            if (link.getTarget() == null) {
                violations.add("Link " + link.getId() + " has null target");
                continue;
            }

            if (!validateConnection(link.getSource(), link.getTarget(), link.getType())) {
                violations.add(String.format(
                    "Invalid link %s: %s (%s) -> %s (%s) via %s",
                    link.getId(),
                    link.getSource().getName(), link.getSource().getType(),
                    link.getTarget().getName(), link.getTarget().getType(),
                    link.getType()
                ));
            }
        }

        // Check for disconnected components
        for (Component component : architecture.getComponents()) {
            boolean isConnected = false;
            for (Link link : architecture.getLinks()) {
                if ((link.getSource() != null && link.getSource().getId().equals(component.getId())) ||
                    (link.getTarget() != null && link.getTarget().getId().equals(component.getId()))) {
                    isConnected = true;
                    break;
                }
            }

            if (!isConnected && architecture.getComponents().size() > 1) {
                warnings.add("Component " + component.getName() + " is not connected to any other components");
            }
        }

        return new ArchitectureValidationResult(violations.isEmpty(), violations, warnings);
    }

    // Inner classes for responses
    public static class ConnectionSuggestion {
        private final boolean canConnect;
        private final List<LinkType> validLinkTypes;
        private final String message;

        public ConnectionSuggestion(boolean canConnect, List<LinkType> validLinkTypes, String message) {
            this.canConnect = canConnect;
            this.validLinkTypes = validLinkTypes;
            this.message = message;
        }

        public boolean isCanConnect() { return canConnect; }
        public List<LinkType> getValidLinkTypes() { return validLinkTypes; }
        public String getMessage() { return message; }
    }

    public static class ArchitectureValidationResult {
        private final boolean valid;
        private final List<String> violations;
        private final List<String> warnings;

        public ArchitectureValidationResult(boolean valid, List<String> violations, List<String> warnings) {
            this.valid = valid;
            this.violations = violations;
            this.warnings = warnings;
        }

        public boolean isValid() { return valid; }
        public List<String> getViolations() { return violations; }
        public List<String> getWarnings() { return warnings; }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
    }
}

