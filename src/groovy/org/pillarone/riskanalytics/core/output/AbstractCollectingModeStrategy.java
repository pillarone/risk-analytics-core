package org.pillarone.riskanalytics.core.output;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.core.packets.PacketList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract public class AbstractCollectingModeStrategy implements ICollectingModeStrategy {

    protected PacketCollector packetCollector;

    private Log LOG = LogFactory.getLog(AbstractCollectingModeStrategy.class);

    /**
     * @param packetList    The first packet within the list is used to try to get none standard period information.
     *                      Period information in following packets is ignored. If no period information is found the
     *                      current period of the packetCollector is used.
     * @param valueMap      field, value map
     * @param valueIndex    Used when aggregating single packets
     * @return
     */
    protected List<SingleValueResultPOJO> createSingleValueResults(PacketList packetList, Map<String, Number> valueMap, int valueIndex) {
        int period = packetCollector.getSimulationScope().getIterationScope().getPeriodScope().getCurrentPeriod();
        if (packetList.size() > 0 && ((Packet) packetList.get(0)).period != null) {
            period = ((Packet) packetList.get(0)).period;
        }
        return createSingleValueResults(valueMap, valueIndex, period);
    }

    /**
     * Create a SingleValueResult object for each packetValue.
     * Information about current simulation is gathered from the scopes.
     * The key of the value map is the field name.
     * If a value is infinite or NaN a log statement is created and the packet ignored.
     */
    private List<SingleValueResultPOJO> createSingleValueResults(Map<String, Number> valueMap, int valueIndex, int period) {
        List<SingleValueResultPOJO> results = new ArrayList(valueMap.size());
        int iteration = packetCollector.getSimulationScope().getIterationScope().getCurrentIteration();
        PathMapping path = packetCollector.getSimulationScope().getMappingCache().lookupPath(packetCollector.getPath());
        for (Map.Entry<String, Number> entry : valueMap.entrySet()) {
            String name = entry.getKey();
            Double value = entry.getValue().doubleValue();
            SingleValueResultPOJO result = new SingleValueResultPOJO();
            if (logInvalidValues(name, value, period, iteration)) continue;
//            result.setSimulationRun(packetCollector.getSimulationScope().getSimulation().getSimulationRun());
            result.setIteration(iteration);
            result.setPeriod(period);
            result.setPath(path);
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
