package org.pillarone.riskanalytics.core.parameterization.global

import java.lang.reflect.Field


class GlobalParameterTarget {

    def targetInstance
    String propertyName

    Class getTargetPropertyType() {
        Field field = getField(targetInstance.getClass(), propertyName)
        return field.getType()
    }

    void setObject(def object) {
        targetInstance[propertyName] = object
    }

    private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            else {
                return getField(superClass, fieldName);
            }
        }
    }
}
