package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.packets.Packet

class AggregatedCollectingModeStrategy extends AbstractCollectingModeStrategy {

    static final String IDENTIFIER = "AGGREGATED"

    private static final String RESOURCE_BUNDLE = "org.pillarone.riskanalytics.core.output.applicationResources"
    private String displayName


    List<SingleValueResult> collect(PacketList packets) {
        Packet p = packets[0]
        Map packetValues = p.valuesToSave
        for (int i = 1; i < packets.size(); i++) {
            Map valuesToAggregate = packets[i].valuesToSave
            for (entry in valuesToAggregate) {
                packetValues[entry.key] = packetValues[entry.key] + entry.value
            }
        }
        return createSingleValueResults(packetValues, 0)
    }

    String getDisplayName(Locale locale) {
        if (displayName == null) {
            displayName = ResourceBundle.getBundle(RESOURCE_BUNDLE, locale).getString("ICollectingModeStrategy.${IDENTIFIER}")
        }
        return displayName;
    }

    String getIdentifier() {
        return IDENTIFIER
    }

}
