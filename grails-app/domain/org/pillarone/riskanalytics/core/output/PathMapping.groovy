package org.pillarone.riskanalytics.core.output

class PathMapping implements Serializable {

    String pathName

    String toString() { pathName }

    long pathID(){
        return id
    }

}