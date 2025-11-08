package com.systemsimulator.service;

import com.systemsimulator.model.*;
import com.systemsimulator.repository.ArchitectureRepository;
import com.systemsimulator.utils.HeuristicAggregator;
import com.systemsimulator.utils.ParameterWeights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ArchitectureService {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureService.class);

    @Autowired
    private ArchitectureRepository architectureRepository;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private HeuristicAggregator heuristicAggregator;

    @Autowired
    private ParameterWeights parameterWeights;

    @Autowired
    private RuleEngineService ruleEngineService;

    /**
     * Check MongoDB connection status
     */
    public boolean isMongoDBConnected() {
        try {
            logger.info("Checking MongoDB connection...");
            long count = architectureRepository.count();
            logger.info("✓ MongoDB connection successful! Total architectures in DB: {}", count);
            return true;
        } catch (Exception e) {
            logger.error("✗ MongoDB connection failed!");
            logger.error("Error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get MongoDB database statistics
     */
    public void logMongoDBStats() {
        try {
            long totalCount = architectureRepository.count();
            long submittedCount = architectureRepository.findBySubmittedTrue().size();

            logger.info("========== MONGODB STATISTICS ==========");
            logger.info("Total architectures in database: {}", totalCount);
            logger.info("Submitted architectures: {}", submittedCount);
            logger.info("Draft architectures: {}", totalCount - submittedCount);
            logger.info("========================================");
        } catch (Exception e) {
            logger.error("Failed to retrieve MongoDB statistics: {}", e.getMessage());
        }
    }

    /**
     * Create a new architecture
     */
    public Architecture createArchitecture(String name) {
        String id = UUID.randomUUID().toString();
        Architecture architecture = new Architecture(id, name);
        return architectureRepository.save(architecture);
    }

    /**
     * Save or update an architecture
     */
    public Architecture saveArchitecture(Architecture architecture) {
        return architectureRepository.save(architecture);
    }

    /**
     * Get architecture by ID
     */
    public Optional<Architecture> getArchitectureById(String id) {
        return architectureRepository.findById(id);
    }

    /**
     * Get all architectures
     */
    public List<Architecture> getAllArchitectures() {
        return architectureRepository.findAll();
    }

    /**
     * Delete architecture by ID
     */
    public void deleteArchitecture(String id) {
        architectureRepository.deleteById(id);
    }

    /**
     * Submit architecture to MongoDB with userId and questionId
     */
    public Architecture submitArchitecture(String architectureId, Integer userId, Integer questionId) {
        logger.info("========== SUBMIT ARCHITECTURE STARTED ==========");
        logger.info("Architecture ID: {}", architectureId);
        logger.info("User ID: {}", userId);
        logger.info("Question ID: {}", questionId);

        try {
            // Step 1: Retrieve architecture
            logger.info("Step 1: Retrieving architecture from repository...");
            Architecture architecture = getArchitectureById(architectureId)
                    .orElseThrow(() -> {
                        logger.error("Architecture not found with ID: {}", architectureId);
                        return new IllegalArgumentException("Architecture not found: " + architectureId);
                    });

            logger.info("✓ Architecture found: {}", architecture.getName());
            logger.info("  - Components: {}", architecture.getComponents().size());
            logger.info("  - Links: {}", architecture.getLinks().size());
            logger.info("  - Created at: {}", architecture.getCreatedAt());

            // Step 2: Update architecture metadata
            logger.info("Step 2: Updating architecture metadata...");
            architecture.setUserId(userId);
            architecture.setQuestionId(questionId);
            architecture.setSubmitted(true);
            architecture.setUpdatedAt(LocalDateTime.now());

            logger.info("✓ Metadata updated:");
            logger.info("  - User ID set to: {}", architecture.getUserId());
            logger.info("  - Question ID set to: {}", architecture.getQuestionId());
            logger.info("  - Submitted flag: {}", architecture.isSubmitted());
            logger.info("  - Updated at: {}", architecture.getUpdatedAt());

            // Step 3: Save to MongoDB
            logger.info("Step 3: Attempting to save to MongoDB Atlas...");
            Architecture savedArchitecture = architectureRepository.save(architecture);

            logger.info("✓ ✓ ✓ SUCCESSFULLY SAVED TO MONGODB ✓ ✓ ✓");
            logger.info("Saved Architecture Details:");
            logger.info("  - ID: {}", savedArchitecture.getId());
            logger.info("  - Name: {}", savedArchitecture.getName());
            logger.info("  - User ID: {}", savedArchitecture.getUserId());
            logger.info("  - Question ID: {}", savedArchitecture.getQuestionId());
            logger.info("  - Components count: {}", savedArchitecture.getComponents().size());
            logger.info("  - Links count: {}", savedArchitecture.getLinks().size());
            logger.info("  - Submitted: {}", savedArchitecture.isSubmitted());
            logger.info("  - Created at: {}", savedArchitecture.getCreatedAt());
            logger.info("  - Updated at: {}", savedArchitecture.getUpdatedAt());

            // Step 4: Verify save by re-reading
            logger.info("Step 4: Verifying save by re-reading from MongoDB...");
            Optional<Architecture> verified = architectureRepository.findById(savedArchitecture.getId());
            if (verified.isPresent()) {
                logger.info("✓ VERIFICATION SUCCESSFUL - Architecture exists in MongoDB");
                logger.info("  - Verified User ID: {}", verified.get().getUserId());
                logger.info("  - Verified Question ID: {}", verified.get().getQuestionId());
                logger.info("  - Verified Submitted flag: {}", verified.get().isSubmitted());
            } else {
                logger.warn("⚠ WARNING: Could not re-read architecture from MongoDB after save");
            }

            logger.info("========== SUBMIT ARCHITECTURE COMPLETED SUCCESSFULLY ==========");
            return savedArchitecture;

        } catch (Exception e) {
            logger.error("========== SUBMIT ARCHITECTURE FAILED ==========");
            logger.error("Error type: {}", e.getClass().getName());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Stack trace:", e);
            throw e;
        }
    }

    /**
     * Get architectures by user ID
     */
    public List<Architecture> getArchitecturesByUserId(Integer userId) {
        return architectureRepository.findByUserId(userId);
    }

    /**
     * Get architectures by question ID
     */
    public List<Architecture> getArchitecturesByQuestionId(Integer questionId) {
        return architectureRepository.findByQuestionId(questionId);
    }

    /**
     * Get submitted architectures
     */
    public List<Architecture> getSubmittedArchitectures() {
        return architectureRepository.findBySubmittedTrue();
    }

    /**
     * Add a component to an architecture
     */
    public Architecture addComponentToArchitecture(String architectureId, Component component) {
        Architecture architecture = getArchitectureById(architectureId)
                .orElseThrow(() -> new IllegalArgumentException("Architecture not found: " + architectureId));

        architecture.addComponent(component);
        return architectureRepository.save(architecture);
    }

    /**
     * Add a link to an architecture
     */
    public Architecture addLinkToArchitecture(String architectureId, Link link) {
        Architecture architecture = getArchitectureById(architectureId)
                .orElseThrow(() -> new IllegalArgumentException("Architecture not found: " + architectureId));

        architecture.addLink(link);
        return architectureRepository.save(architecture);
    }

    /**
     * Add a component to an architecture by component ID
     */
    public Architecture addComponentToArchitectureById(String architectureId, String componentId) {
        Architecture architecture = getArchitectureById(architectureId)
                .orElseThrow(() -> new IllegalArgumentException("Architecture not found: " + architectureId));

        Component component = componentService.getComponentById(componentId)
                .orElseThrow(() -> new IllegalArgumentException("Component not found: " + componentId));

        architecture.addComponent(component);
        return architectureRepository.save(architecture);
    }

    /**
     * Add a link to an architecture by link ID
     */
    public Architecture addLinkToArchitectureById(String architectureId, String linkId) {
        Architecture architecture = getArchitectureById(architectureId)
                .orElseThrow(() -> new IllegalArgumentException("Architecture not found: " + architectureId));

        Link link = linkService.getLinkById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found: " + linkId));

        architecture.addLink(link);
        return architectureRepository.save(architecture);
    }

    /**
     * Evaluate architecture and return overall score
     */
    public double evaluateArchitecture(String architectureId) {
        Architecture architecture = getArchitectureById(architectureId)
                .orElseThrow(() -> new IllegalArgumentException("Architecture not found: " + architectureId));

        return heuristicAggregator.aggregate(
                architecture.getComponents(),
                architecture.getLinks(),
                parameterWeights
        );
    }

    /**
     * Evaluate architecture with detailed results
     */
    public ArchitectureEvaluation evaluateArchitectureDetailed(String architectureId) {
        Architecture architecture = getArchitectureById(architectureId)
                .orElseThrow(() -> new IllegalArgumentException("Architecture not found: " + architectureId));

        // Calculate overall score
        double overallScore = heuristicAggregator.aggregate(
                architecture.getComponents(),
                architecture.getLinks(),
                parameterWeights
        );

        // Calculate parameter-specific scores
        Map<Parameter, Double> parameterScores = heuristicAggregator.aggregateByParameter(
                architecture.getComponents()
        );

        // Identify bottlenecks
        List<BottleneckInfo> bottlenecks = identifyBottlenecks(architecture);

        // Generate insights
        List<String> insights = generateInsights(architecture, overallScore, parameterScores, bottlenecks);

        // Validate architecture
        RuleEngineService.ArchitectureValidationResult validation =
                ruleEngineService.validateArchitecture(architecture);

        return new ArchitectureEvaluation(
                architectureId,
                architecture.getName(),
                overallScore,
                architecture.getComponents().size(),
                architecture.getLinks().size(),
                parameterScores,
                bottlenecks,
                insights,
                validation.isValid(),
                validation.getViolations(),
                validation.getWarnings()
        );
    }

    /**
     * Identify bottleneck components in the architecture
     */
    private List<BottleneckInfo> identifyBottlenecks(Architecture architecture) {
        List<BottleneckInfo> bottlenecks = new ArrayList<>();

        for (Component component : architecture.getComponents()) {
            double bottleneckScore = heuristicAggregator.calculateBottleneckScore(
                    component,
                    architecture.getLinks()
            );

            if (bottleneckScore < 0.8) {
                LinkService.ConnectionStats stats = linkService.getConnectionStats(component.getId());
                bottlenecks.add(new BottleneckInfo(
                        component.getId(),
                        component.getName(),
                        component.getType(),
                        bottleneckScore,
                        stats.getIncomingLinks(),
                        stats.getOutgoingLinks()
                ));
            }
        }

        return bottlenecks;
    }

    /**
     * Generate insights and recommendations for the architecture
     */
    private List<String> generateInsights(Architecture architecture,
                                          double overallScore,
                                          Map<Parameter, Double> parameterScores,
                                          List<BottleneckInfo> bottlenecks) {
        List<String> insights = new ArrayList<>();

        // Overall score assessment
        if (overallScore >= 8.0) {
            insights.add("✅ Excellent architecture design with strong performance characteristics.");
        } else if (overallScore >= 6.5) {
            insights.add("✓ Good architecture design. Consider optimizations for better performance.");
        } else if (overallScore >= 5.0) {
            insights.add("⚠ Architecture is functional but has room for improvement.");
        } else {
            insights.add("❌ Architecture needs significant improvements. Review component choices and connections.");
        }

        // Component count assessment
        int componentCount = architecture.getComponents().size();
        if (componentCount == 0) {
            insights.add("❌ Architecture has no components. Add components to build your system.");
        } else if (componentCount == 1) {
            insights.add("⚠ Architecture has only one component. Consider adding more components for scalability.");
        } else if (componentCount > 15) {
            insights.add("⚠ Architecture is complex with " + componentCount + " components. Ensure maintainability.");
        }

        // Link assessment
        int linkCount = architecture.getLinks().size();
        if (linkCount == 0 && componentCount > 1) {
            insights.add("❌ Components are not connected. Add links to establish data flow.");
        } else if (linkCount > 0) {
            double ratio = (double) linkCount / componentCount;
            if (ratio < 1.0) {
                insights.add("⚠ Architecture is under-connected. Consider adding more links for redundancy.");
            } else if (ratio > 4.0) {
                insights.add("⚠ Architecture may be over-connected. Simplify if possible to reduce complexity.");
            }
        }

        // Parameter-specific insights
        if (parameterScores.containsKey(Parameter.LATENCY)) {
            double latency = parameterScores.get(Parameter.LATENCY);
            if (latency < 5.0) {
                insights.add("⚠ Low latency score. Consider adding caching layers or using faster storage.");
            } else if (latency >= 8.0) {
                insights.add("✅ Excellent latency characteristics. System should be responsive.");
            }
        }

        if (parameterScores.containsKey(Parameter.AVAILABILITY)) {
            double availability = parameterScores.get(Parameter.AVAILABILITY);
            if (availability < 6.0) {
                insights.add("⚠ Low availability score. Add replication and redundancy for high availability.");
            } else if (availability >= 8.5) {
                insights.add("✅ Strong availability design. System should handle failures well.");
            }
        }

        if (parameterScores.containsKey(Parameter.SCALABILITY)) {
            double scalability = parameterScores.get(Parameter.SCALABILITY);
            if (scalability < 6.0) {
                insights.add("⚠ Limited scalability. Consider using load balancers and horizontal scaling.");
            } else if (scalability >= 8.5) {
                insights.add("✅ Highly scalable architecture. Can handle traffic growth effectively.");
            }
        }

        if (parameterScores.containsKey(Parameter.COST)) {
            double cost = parameterScores.get(Parameter.COST);
            if (cost < 5.0) {
                insights.add("💰 High cost architecture. Review component choices for cost optimization.");
            } else if (cost >= 7.5) {
                insights.add("✅ Cost-effective architecture design.");
            }
        }

        // Bottleneck insights
        if (!bottlenecks.isEmpty()) {
            insights.add("⚠ Detected " + bottlenecks.size() + " potential bottleneck(s):");
            for (BottleneckInfo bottleneck : bottlenecks) {
                insights.add("  • " + bottleneck.getComponentName() + " (" + bottleneck.getComponentType() +
                        ") has " + bottleneck.getTotalConnections() + " connections. Consider load balancing or caching.");
            }
        }

        // Architecture pattern detection
        detectArchitecturePatterns(architecture, insights);

        return insights;
    }

    /**
     * Detect and suggest architecture patterns
     */
    private void detectArchitecturePatterns(Architecture architecture, List<String> insights) {
        boolean hasLoadBalancer = false;
        boolean hasCache = false;
        boolean hasQueue = false;
        boolean hasDatabase = false;

        for (Component component : architecture.getComponents()) {
            if (component.getType() == ComponentType.LOAD_BALANCER) hasLoadBalancer = true;
            if (component.getType() == ComponentType.CACHE) hasCache = true;
            if (component.getType() == ComponentType.QUEUE) hasQueue = true;
            if (component.getType() == ComponentType.DATABASE) hasDatabase = true;
        }

        // Suggestions based on missing components
        if (hasDatabase && !hasCache) {
            insights.add("💡 Consider adding a cache layer to improve database performance.");
        }

        if (architecture.getComponents().size() > 3 && !hasLoadBalancer) {
            insights.add("💡 Consider adding a load balancer for better traffic distribution.");
        }

        if (hasDatabase && hasCache && hasQueue) {
            insights.add("✅ Architecture includes database, cache, and queue - good for scalable systems.");
        }
    }

    /**
     * Compare two architectures
     */
    public ArchitectureComparison compareArchitectures(String arch1Id, String arch2Id) {
        Architecture arch1 = getArchitectureById(arch1Id)
                .orElseThrow(() -> new IllegalArgumentException("Architecture 1 not found: " + arch1Id));
        Architecture arch2 = getArchitectureById(arch2Id)
                .orElseThrow(() -> new IllegalArgumentException("Architecture 2 not found: " + arch2Id));

        double score1 = heuristicAggregator.aggregate(arch1.getComponents(), arch1.getLinks(), parameterWeights);
        double score2 = heuristicAggregator.aggregate(arch2.getComponents(), arch2.getLinks(), parameterWeights);

        Map<Parameter, Double> params1 = heuristicAggregator.aggregateByParameter(arch1.getComponents(), arch1.getLinks());
        Map<Parameter, Double> params2 = heuristicAggregator.aggregateByParameter(arch2.getComponents(), arch2.getLinks());
        return new ArchitectureComparison(
                arch1.getId(), arch1.getName(), score1,
                arch2.getId(), arch2.getName(), score2,
                params1, params2
        );
    }

    // Inner classes for responses
    public static class ArchitectureEvaluation {
        private final String architectureId;
        private final String architectureName;
        private final double overallScore;
        private final int componentCount;
        private final int linkCount;
        private final Map<Parameter, Double> parameterScores;
        private final List<BottleneckInfo> bottlenecks;
        private final List<String> insights;
        private final boolean valid;
        private final List<String> violations;
        private final List<String> warnings;

        public ArchitectureEvaluation(String architectureId, String architectureName,
                                      double overallScore, int componentCount, int linkCount,
                                      Map<Parameter, Double> parameterScores,
                                      List<BottleneckInfo> bottlenecks,
                                      List<String> insights,
                                      boolean valid,
                                      List<String> violations,
                                      List<String> warnings) {
            this.architectureId = architectureId;
            this.architectureName = architectureName;
            this.overallScore = overallScore;
            this.componentCount = componentCount;
            this.linkCount = linkCount;
            this.parameterScores = parameterScores;
            this.bottlenecks = bottlenecks;
            this.insights = insights;
            this.valid = valid;
            this.violations = violations;
            this.warnings = warnings;
        }

        // Getters
        public String getArchitectureId() { return architectureId; }
        public String getArchitectureName() { return architectureName; }
        public double getOverallScore() { return overallScore; }
        public int getComponentCount() { return componentCount; }
        public int getLinkCount() { return linkCount; }
        public Map<Parameter, Double> getParameterScores() { return parameterScores; }
        public List<BottleneckInfo> getBottlenecks() { return bottlenecks; }
        public List<String> getInsights() { return insights; }
        public boolean isValid() { return valid; }
        public List<String> getViolations() { return violations; }
        public List<String> getWarnings() { return warnings; }
    }

    public static class BottleneckInfo {
        private final String componentId;
        private final String componentName;
        private final ComponentType componentType;
        private final double bottleneckScore;
        private final int incomingConnections;
        private final int outgoingConnections;

        public BottleneckInfo(String componentId, String componentName, ComponentType componentType,
                              double bottleneckScore, int incomingConnections, int outgoingConnections) {
            this.componentId = componentId;
            this.componentName = componentName;
            this.componentType = componentType;
            this.bottleneckScore = bottleneckScore;
            this.incomingConnections = incomingConnections;
            this.outgoingConnections = outgoingConnections;
        }

        public String getComponentId() { return componentId; }
        public String getComponentName() { return componentName; }
        public ComponentType getComponentType() { return componentType; }
        public double getBottleneckScore() { return bottleneckScore; }
        public int getIncomingConnections() { return incomingConnections; }
        public int getOutgoingConnections() { return outgoingConnections; }
        public int getTotalConnections() { return incomingConnections + outgoingConnections; }
    }

    public static class ArchitectureComparison {
        private final String arch1Id;
        private final String arch1Name;
        private final double arch1Score;
        private final String arch2Id;
        private final String arch2Name;
        private final double arch2Score;
        private final Map<Parameter, Double> arch1Parameters;
        private final Map<Parameter, Double> arch2Parameters;

        public ArchitectureComparison(String arch1Id, String arch1Name, double arch1Score,
                                      String arch2Id, String arch2Name, double arch2Score,
                                      Map<Parameter, Double> arch1Parameters,
                                      Map<Parameter, Double> arch2Parameters) {
            this.arch1Id = arch1Id;
            this.arch1Name = arch1Name;
            this.arch1Score = arch1Score;
            this.arch2Id = arch2Id;
            this.arch2Name = arch2Name;
            this.arch2Score = arch2Score;
            this.arch1Parameters = arch1Parameters;
            this.arch2Parameters = arch2Parameters;
        }

        // Getters
        public String getArch1Id() { return arch1Id; }
        public String getArch1Name() { return arch1Name; }
        public double getArch1Score() { return arch1Score; }
        public String getArch2Id() { return arch2Id; }
        public String getArch2Name() { return arch2Name; }
        public double getArch2Score() { return arch2Score; }
        public Map<Parameter, Double> getArch1Parameters() { return arch1Parameters; }
        public Map<Parameter, Double> getArch2Parameters() { return arch2Parameters; }
        public double getScoreDifference() { return arch1Score - arch2Score; }
        public String getWinner() {
            return arch1Score > arch2Score ? arch1Name :
                    arch2Score > arch1Score ? arch2Name : "Tie";
        }
    }
}
