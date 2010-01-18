package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.PacketList

class SingleValueCollectingModeStrategy extends AbstractCollectingModeStrategy {

    static final String IDENTIFIER = "SINGLE"

    private static final String RESOURCE_BUNDLE = "org.pillarone.riskanalytics.core.output.applicationResources"
    private String displayName

    List<SingleValueResult> collect(PacketList results) {
        List result = []
        int valueIndex = 0
        for (Packet p in results) {
            result.addAll(createSingleValueResults(p.valuesToSave, valueIndex))
            valueIndex++
        }
        return result;
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
