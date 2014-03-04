package org.pillarone.riskanalytics.core.output

class CollectorMapping implements Serializable {

    String collectorName

    @Override
    String toString() {
        collectorName
    }

    static constraints = {
        collectorName(unique: true)
    }

}