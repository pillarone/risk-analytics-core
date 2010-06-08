package org.pillarone.riskanalytics.core.output;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract public class AbstractCollectingModeStrategy implements ICollectingModeStrategy {

    protected PacketCollector packetCollector;

    private Log LOG = LogFactory.getLog(AbstractCollectingModeStrategy.class);

    /**
     * Create a SingleValueResult object for each packetValue.
     * Information about current simulation is gathered from the scopes.
     * The key of the value map is the field name.
     * If a value is infinite or NaN a log statement is created and the packet ignored.
     */
    protected List<SingleValueResultPOJO> createSingleValueResults(Map<String, Number> valueMap, int valueIndex) {
        List<SingleValueResultPOJO> results = new ArrayList(valueMap.size());
        for (Map.Entry<String, Number> entry : valueMap.entrySet()) {
            String name = entry.getKey();
            Double value = entry.getValue().doubleValue();
            SingleValueResultPOJO result = new SingleValueResultPOJO();
            int period = packetCollector.getSimulationScope().getIterationScope().getPeriodScope().getCurrentPeriod();
            int iteration = packetCollector.getSimulationScope().getIterationScope().getCurrentIteration();
            if (logInvalidValues(name, value, period, iteration)) continue;
            result.setSimulationRun(packetCollector.getSimulationScope().getSimulation().getSimulationRun());
            result.setIteration(iteration);
            result.setPeriod(period);
            result.setPath(packetCollector.getSimulationScope().getMappingCache().lookupPath(packetCollector.getPath()));
            result.setCollector(packetCollector.getSimulationScope().getMappingCache().lookupCollector(packetCollector.getMode().getIdentifier()));
            result.setField(packetCollector.getSimulationScope().getMappingCache().lookupField(name));
            result.setValueIndex(valueIndex);
            result.setValue(value);
            results.add(result);
        }
        return results;
    }

    private boolean logInvalidValues(String name, Double value, int period, int iteration) {
        if (value.isInfinite() || value.isNaN()) {
            if (LOG.isErrorEnabled()) {
                StringBuilder message = new StringBuilder();
                message.append(value).append(" collected at ").append(packetCollector.getPath()).append(":").append(name);
                message.append(" (period ").append(period).append(") in iteration ");
                message.append(iteration).append(" - ignoring.");
                LOG.info(message);
            }
            return true;
        }
        return false;
    }

    public PacketCollector getPacketCollector() {
        return packetCollector;
    }

    public void setPacketCollector(PacketCollector packetCollector) {
        this.packetCollector = packetCollector;
    }
}
