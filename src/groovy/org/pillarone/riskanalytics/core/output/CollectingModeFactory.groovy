package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class CollectingModeFactory {

    private static Map<String, Class> strategies = new HashMap()
    private static Log LOG = LogFactory.getLog(CollectingModeFactory)

    static void registerStrategy(ICollectingModeStrategy strategy) {
        String identifier = strategy.identifier
        Class existingClass = strategies.get(identifier)
        if (existingClass == null) {
            strategies.put(identifier, strategy.getClass())
        } else {
            if (existingClass.name == strategy.getClass().name) {
                LOG.warn "Collecting mode strategy $identifier already exists - ignoring"
            } else {
                throw new IllegalStateException("Identifier $identifier already associated with ${existingClass.name}")
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
        Class clazz = strategies.get(identifier)
        return (ICollectingModeStrategy) clazz?.newInstance()
    }

}
