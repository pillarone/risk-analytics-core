package org.pillarone.riskanalytics.core

class ModelDAO {

    String name
    String modelClassName
    String srcCode
    String itemVersion

    static mapping = {
        srcCode lazy: true, type: 'text'
    }

    static constraints = {
        srcCode(size: 0..32672)
    }
}    