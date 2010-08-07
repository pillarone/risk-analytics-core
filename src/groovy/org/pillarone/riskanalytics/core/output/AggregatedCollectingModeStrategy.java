package org.pillarone.riskanalytics.core.output;

import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.packets.Packet;

import java.util.*;

public class AggregatedCollectingModeStrategy extends AbstractCollectingModeStrategy {

    static final String IDENTIFIER = "AGGREGATED";

    private static final String RESOURCE_BUNDLE = "org.pillarone.riskanalytics.core.output.applicationResources";
    private String displayName;


    public List<SingleValueResultPOJO> collect(PacketList packets) throws IllegalAccessException {
        Packet p = (Packet) packets.get(0);
        Map packetValues = p.getValuesToSave();
        for (int i = 1; i < packets.size(); i++) {
            p = (Packet) packets.get(i);
            Map valuesToAggregate = p.getValuesToSave();
            Set<Map.Entry> entrySet = valuesToAggregate.entrySet();
            for (Map.Entry entry : entrySet) {
                packetValues.put(entry.getKey(), ((Double)packetValues.get(entry.getKey())) + ((Double)entry.getValue()));
            }
        }
        return createSingleValueResults(packets, packetValues, 0);
    }

    public String getDisplayName(Locale locale) {
        if (displayName == null) {
            displayName = ResourceBundle.getBundle(RESOURCE_BUNDLE, locale).getString("ICollectingModeStrategy."+IDENTIFIER);
        }
        return displayName;
    }

    public String getIdentifier() {
        return IDENTIFIER;
    }

}
