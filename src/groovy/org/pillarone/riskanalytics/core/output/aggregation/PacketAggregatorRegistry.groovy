package org.pillarone.riskanalytics.core.output.aggregation

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class PacketAggregatorRegistry {

    private static Log LOG = LogFactory.getLog(PacketAggregatorRegistry)
    private static HashMap<Class, IPacketAggregator> aggregatorMap = [:]

    public static void registerAggregator(Class packetClass, IPacketAggregator aggregator) {
        IPacketAggregator existing = aggregatorMap.get(packetClass)
        if(existing != null) {
            if(existing.class == aggregator.class) {
                LOG.warn("Aggregator for $packetClass.name already registered.")
            } else {
                throw new IllegalStateException("Already an aggregator registered for ${packetClass.name}: ${existing.class.name}")
            }
        }

        aggregatorMap.put(packetClass, aggregator)
    }

    public static IPacketAggregator getAggregator(Class packetClass) {
        if (aggregatorMap.empty) {
            throw new IllegalStateException("No aggregators registered.")
        }
        Class currentClass = packetClass
        while (currentClass != Object) {
            for (Map.Entry<Class, IPacketAggregator> entry in aggregatorMap.entrySet()) {
                if (entry.key == currentClass) {
                    return entry.value
                }
            }
            currentClass = currentClass.superclass
        }

        throw new IllegalStateException("No aggregator found for $packetClass.name")
    }

}
