package org.pillarone.riskanalytics.core.output

abstract class AbstractCollectingModeStrategy implements ICollectingModeStrategy {

    PacketCollector packetCollector

    /**
     * Create a SingleValueResult object for each packetValue.
     * Information about current simulation is gathered from the scopes.
     * The key of the value map is the field name.
     */
    protected List createSingleValueResults(Map<String, Number> valueMap, int valueIndex) {
        List results = []
        for (entry in valueMap) {
            String name = entry.key
            Double value = entry.value
            SingleValueResult result = new SingleValueResult()
            result.simulationRun = packetCollector.simulationScope.simulation.simulationRun
            result.iteration = packetCollector.simulationScope.iterationScope.currentIteration
            result.period = packetCollector.simulationScope.iterationScope.periodScope.currentPeriod
            result.path = packetCollector.pathMapping()
            result.collector = packetCollector.collectorMapping()
            result.field = packetCollector.getFieldMapping(name)
            result.valueIndex = valueIndex
            result.value = value
            results << result
        }
        return results
    }
}
