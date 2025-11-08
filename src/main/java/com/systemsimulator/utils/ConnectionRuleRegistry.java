package com.systemsimulator.utils;

import com.systemsimulator.model.ConnectionRule;
import com.systemsimulator.model.LinkType;
import com.systemsimulator.model.rules.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConnectionRuleRegistry {
    private final Map<LinkType, List<ConnectionRule>> rulesByLinkType = new HashMap<>();
    private final List<ConnectionRule> allRules = new ArrayList<>();

    public ConnectionRuleRegistry() {
        registerDefaultRules();
    }

    /**
     * Register all default connection rules
     */
    private void registerDefaultRules() {
        // Register all 8 connection rule types
        registerRule(new ApiCallRule());
        registerRule(new StreamRule());
        registerRule(new ReplicationRule());
        registerRule(new EtlPipelineRule());
        registerRule(new BatchTransferRule());
        registerRule(new EventFlowRule());
        registerRule(new CacheLookupRule());
        registerRule(new DatabaseQueryRule());
    }

    /**
     * Register a new connection rule
     */
    public void registerRule(ConnectionRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Rule cannot be null");
        }

        allRules.add(rule);
        rulesByLinkType.computeIfAbsent(rule.getLinkType(), k -> new ArrayList<>()).add(rule);
    }

    /**
     * Get all rules for a specific link type
     */
    public List<ConnectionRule> getRulesForLinkType(LinkType linkType) {
        return rulesByLinkType.getOrDefault(linkType, Collections.emptyList());
    }

    /**
     * Get all registered rules
     */
    public List<ConnectionRule> getAllRules() {
        return new ArrayList<>(allRules);
    }

    /**
     * Check if rules exist for a link type
     */
    public boolean hasRuleFor(LinkType linkType) {
        return rulesByLinkType.containsKey(linkType);
    }

    /**
     * Get count of rules for a link type
     */
    public int getRuleCount(LinkType linkType) {
        return getRulesForLinkType(linkType).size();
    }

    /**
     * Get total number of registered rules
     */
    public int getTotalRuleCount() {
        return allRules.size();
    }

    /**
     * Get all link types that have registered rules
     */
    public Set<LinkType> getSupportedLinkTypes() {
        return rulesByLinkType.keySet();
    }

    /**
     * Remove all rules for a specific link type
     */
    public void clearRulesForLinkType(LinkType linkType) {
        List<ConnectionRule> rules = rulesByLinkType.remove(linkType);
        if (rules != null) {
            allRules.removeAll(rules);
        }
    }

    /**
     * Clear all registered rules
     */
    public void clearAllRules() {
        rulesByLinkType.clear();
        allRules.clear();
    }

    /**
     * Reset to default rules
     */
    public void resetToDefaults() {
        clearAllRules();
        registerDefaultRules();
    }

    /**
     * Get rule descriptions for all registered rules
     */
    public Map<LinkType, List<String>> getRuleDescriptions() {
        Map<LinkType, List<String>> descriptions = new HashMap<>();

        for (Map.Entry<LinkType, List<ConnectionRule>> entry : rulesByLinkType.entrySet()) {
            List<String> ruleDescriptions = new ArrayList<>();
            for (ConnectionRule rule : entry.getValue()) {
                ruleDescriptions.add(rule.getDescription());
            }
            descriptions.put(entry.getKey(), ruleDescriptions);
        }

        return descriptions;
    }

    /**
     * Check if a specific rule type is registered
     */
    public boolean isRuleRegistered(Class<? extends ConnectionRule> ruleClass) {
        for (ConnectionRule rule : allRules) {
            if (rule.getClass().equals(ruleClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get registry statistics
     */
    public RegistryStats getStats() {
        Map<LinkType, Integer> ruleCountsByType = new HashMap<>();
        for (Map.Entry<LinkType, List<ConnectionRule>> entry : rulesByLinkType.entrySet()) {
            ruleCountsByType.put(entry.getKey(), entry.getValue().size());
        }

        return new RegistryStats(
            allRules.size(),
            rulesByLinkType.size(),
            ruleCountsByType
        );
    }

    /**
     * Inner class for registry statistics
     */
    @Getter
    public static class RegistryStats {
        private final int totalRules;
        private final int supportedLinkTypes;
        private final Map<LinkType, Integer> ruleCountsByType;

        public RegistryStats(int totalRules, int supportedLinkTypes, Map<LinkType, Integer> ruleCountsByType) {
            this.totalRules = totalRules;
            this.supportedLinkTypes = supportedLinkTypes;
            this.ruleCountsByType = ruleCountsByType;
        }

        @Override
        public String toString() {
            return String.format("RegistryStats{totalRules=%d, supportedLinkTypes=%d}",
                               totalRules, supportedLinkTypes);
        }
    }
}
