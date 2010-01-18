package org.pillarone.riskanalytics.core.output

public class CollectorInformation {

    String collectingStrategyIdentifier
    PathMapping path

    static belongsTo = [configuration: ResultConfigurationDAO]
}
