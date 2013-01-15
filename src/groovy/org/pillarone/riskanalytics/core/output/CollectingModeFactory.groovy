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

    /**
     * SCP 15.01.2012
     * Note that I'm not that keen on returning null out of this. However, I need to get on. Disucssion with MSP indicates
     * potential performance impact if we simply register a 'NONE' collecting strategy. I have to get on so I'm simply preserving
     * the existing (pre-refactoring) behaviour. I think it's wrong however. At the least I think the method name should inform the caller
     * that it must deal will a null return value. NullPointerExceptions are hard to explain to the userbase.
     *
     * Will lookup a CollectingModeStrategy against the list of registered collectors.
     * @param identifier String identifying the collectors registered with this class
     * @return either the collecting mode strategy or null
     */
    static ICollectingModeStrategy getStrategy(String identifier) {
        ICollectingModeStrategy strategy = strategies.get(identifier)
        if(strategy == null) {
            return null
        }
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
