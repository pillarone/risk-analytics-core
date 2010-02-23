package org.pillarone.riskanalytics.core.output;

import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.core.packets.PacketList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class SingleValueCollectingModeStrategy extends AbstractCollectingModeStrategy {

    static final String IDENTIFIER = "SINGLE";

    private static final String RESOURCE_BUNDLE = "org.pillarone.riskanalytics.core.output.applicationResources";
    private String displayName;

    public List<SingleValueResult> collect(PacketList results) throws IllegalAccessException {
        List<SingleValueResult> result = new ArrayList<SingleValueResult>(results.size());
        int valueIndex = 0;
        for (Object p : results) {
            result.addAll(createSingleValueResults(((Packet)p).getValuesToSave(), valueIndex));
            valueIndex++;
        }
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

}
