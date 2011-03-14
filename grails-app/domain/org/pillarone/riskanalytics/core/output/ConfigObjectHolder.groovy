package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.util.GroovyUtils

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
        ConfigObject result
        GroovyUtils.parseGroovyScript data, { ConfigObject config ->
            result = config
        }
        return result
    }

}