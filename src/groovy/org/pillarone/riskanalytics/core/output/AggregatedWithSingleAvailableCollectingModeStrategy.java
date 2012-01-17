package org.pillarone.riskanalytics.core.output;

public class AggregatedWithSingleAvailableCollectingModeStrategy extends AggregatedCollectingModeStrategy {

    public static final String IDENTIFIER = "AGGREGATED_SINGLE_AVAILABLE";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean isCompatibleWith(Class packetClass) {
        return false; //don't show in UI - used internally only
    }
}
