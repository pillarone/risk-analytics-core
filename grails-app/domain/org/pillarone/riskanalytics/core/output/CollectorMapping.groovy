package org.pillarone.riskanalytics.core.output

public class CollectorMapping implements Serializable {
    String collectorName

    //Needed if we want get the id from java code
    Long getId() {
        return id
    }
}