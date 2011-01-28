package org.pillarone.riskanalytics.core.parameterization.global

import org.pillarone.riskanalytics.core.components.Component
import java.lang.reflect.Method
import org.pillarone.riskanalytics.core.util.PacketUtils


class GlobalParameterSource {

    String identifier
    Component source
    Method method

    void applyToTarget(GlobalParameterTarget target) {
        checkTypes(target)
        target.object = method.invoke(source, null)
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
