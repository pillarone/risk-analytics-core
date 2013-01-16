package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class CollectingModeFactory {

    private static Map<String, ICollectingModeStrategy> strategies = new HashMap()
    private static Log LOG = LogFactory.getLog(CollectingModeFactory)

    static void registerStrategy(ICollectingModeStrategy strategy) {
        String identifier = strategy.identifier
        ICollectingModeStrategy existingStrategy = strategies.get(identifier)
        if (existingStrategy == null) {
            strategies.put(identifier, strategy)
        } else {
            if (existingStrategy.class.name == strategy.class.name) {
                LOG.warn "Collecting mode strategy $identifier already exists - ignoring"
            } else {
                throw new IllegalStateException("Identifier $identifier already associated with ${existingStrategy.class.name}")
            }
        }
    }

    static List getAvailableStrategies() {
        List results = []
        for (String identifier in strategies.keySet()) {
            results << getStrategy(identifier)
        }
        return results
    }

    static ICollectingModeStrategy getStrategy(String identifier) {
        ICollectingModeStrategy strategy = strategies.get(identifier)
        return getNewInstance(strategy)
    }

    static ICollectingModeStrategy getNewInstance(ICollectingModeStrategy strategy) {
        return (ICollectingModeStrategy) strategy.class.newInstance(strategy.arguments)
    }

    static List<ICollectingModeStrategy> getDrillDownStrategies(DrillDownMode drillDownMode) {
        List<ICollectingModeStrategy> result = []
        for (ICollectingModeStrategy strategy: strategies.values()){
            if (strategy.drillDownModes.contains(drillDownMode)){
                result << strategy
            }
        }
        return result
    }
}
