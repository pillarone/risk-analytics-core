package org.pillarone.riskanalytics.core.output

public class PathMapping {
    String pathName

    String toString() { pathName }

    //Needed if we want get the id from java code
    Long getId() {
        return id
    }
}