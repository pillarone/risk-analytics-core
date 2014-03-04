package org.pillarone.riskanalytics.core.output

class CollectorInformation {

    String collectingStrategyIdentifier
    PathMapping path

    static belongsTo = [configuration: ResultConfigurationDAO]
}
