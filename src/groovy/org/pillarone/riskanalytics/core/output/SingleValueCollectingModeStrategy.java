package org.pillarone.riskanalytics.core.output;

import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.core.packets.PacketList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class SingleValueCollectingModeStrategy extends AbstractCollectingModeStrategy {

    public static final String IDENTIFIER = "SINGLE";

    private static final String RESOURCE_BUNDLE = "org.pillarone.riskanalytics.core.output.applicationResources";
    private String displayName;

    private AggregatedCollectingModeStrategy aggregatedCollectingMode;

    public SingleValueCollectingModeStrategy() {
        aggregatedCollectingMode = new AggregatedWithSingleAvailableCollectingModeStrategy();
    }

    public SingleValueCollectingModeStrategy(boolean crashSimOnError) {
        super(crashSimOnError);
        aggregatedCollectingMode = new AggregatedWithSingleAvailableCollectingModeStrategy();
    }

    public List<SingleValueResultPOJO> collect(PacketList results) throws IllegalAccessException {
        List<SingleValueResultPOJO> result = new ArrayList<SingleValueResultPOJO>(results.size());
        int valueIndex = 0;
        for (Object p : results) {
            result.addAll(createSingleValueResults((Packet) p, ((Packet)p).getValuesToSave(), valueIndex));
            valueIndex++;
        }
        final List<SingleValueResultPOJO> aggregatedValues = aggregatedCollectingMode.collect(results);
        for (SingleValueResultPOJO singleValueResult : aggregatedValues) {
            singleValueResult.setCollector(packetCollector.getSimulationScope().getMappingCache().lookupCollector(aggregatedCollectingMode.getIdentifier()));
        }
        result.addAll(aggregatedValues);
        return result;
    }

    public String getDisplayName(Locale locale) {
        if (displayName == null) {
            displayName = ResourceBundle.getBundle(RESOURCE_BUNDLE, locale).getString("ICollectingModeStrategy." + IDENTIFIER);
        }
        return displayName;
    }

    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setPacketCollector(PacketCollector packetCollector) {
        super.setPacketCollector(packetCollector);
        aggregatedCollectingMode.setPacketCollector(packetCollector);
    }

    public boolean isCompatibleWith(Class packetClass) {
        return Packet.class.isAssignableFrom(packetClass);
    }
}
