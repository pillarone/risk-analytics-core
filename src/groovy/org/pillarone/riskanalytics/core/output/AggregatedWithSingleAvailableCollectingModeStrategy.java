package org.pillarone.riskanalytics.core.output;

public class AggregatedWithSingleAvailableCollectingModeStrategy extends AggregatedCollectingModeStrategy {

    public static final String IDENTIFIER = "AGGREGATED_SINGLE_AVAILABLE";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
