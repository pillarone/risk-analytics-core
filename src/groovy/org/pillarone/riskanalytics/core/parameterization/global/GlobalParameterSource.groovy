package org.pillarone.riskanalytics.core.parameterization.global

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.components.Component
import java.lang.reflect.Method
import org.pillarone.riskanalytics.core.util.PacketUtils
import java.lang.reflect.InvocationTargetException

@CompileStatic
class GlobalParameterSource {

    String identifier
    Component source
    transient Method method

    void applyToTarget(GlobalParameterTarget target) {
        checkTypes(target)
        try {
            target.object = method.invoke(source)
        }
        catch (InvocationTargetException ex) {
            throw new IllegalArgumentException("Problems with applying source " + source.getName() + ". A runtime parameter (" + identifier + ") might be null.", ex)
        }
    }

    void checkTypes(GlobalParameterTarget target) {
        Class<?> returnType = convertPrimitive(method.returnType)
        if (!convertPrimitive(target.getTargetPropertyType()).isAssignableFrom(returnType)) {
            throw new IllegalStateException("Cannot assing global parameter $identifier with type ${returnType.simpleName} to $target.propertyName with class ${target.targetPropertyType} in ${target.targetInstance.class.simpleName}")
        }
    }

    private Class convertPrimitive(Class clazz) {
        if (clazz.isPrimitive()) {
            clazz = PacketUtils.primitiveToWrapperClass.get(clazz)
        }
        return clazz
    }
}
