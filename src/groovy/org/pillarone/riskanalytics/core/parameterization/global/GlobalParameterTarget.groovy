package org.pillarone.riskanalytics.core.parameterization.global

import java.lang.reflect.Field


class GlobalParameterTarget {

    def targetInstance
    String propertyName

    Class getTargetPropertyType() {
        Field field = targetInstance.getClass().getDeclaredField(propertyName)
        return field.getType()
    }

    void setObject(def object) {
        targetInstance[propertyName] = object
    }
}
