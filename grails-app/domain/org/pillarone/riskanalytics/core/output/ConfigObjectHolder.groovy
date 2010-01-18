package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.ModelStructureDAO

class ConfigObjectHolder {

    String data

    static belongsTo = ModelStructureDAO

    static constraints = {
        data(size: 0..32672)
    }

    static mapping = {
        data type: 'text'
    }

    public ConfigObject asConfigObject() {
        return new ConfigSlurper().parse(data)
    }

}