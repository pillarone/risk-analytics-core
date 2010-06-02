package org.pillarone.riskanalytics.core.output

public class FieldMapping implements Serializable {
    String fieldName

    //Needed if we want get the id from java code
    Long getId() {
        return id
    }
}