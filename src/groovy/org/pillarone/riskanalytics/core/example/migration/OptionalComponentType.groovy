package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class OptionalComponentType extends AbstractParameterObjectClassifier {

    public static final OptionalComponentType ENABLED = new OptionalComponentType('enabled', 'ENABLED',
        [timeMode : TimeMode.PERIOD,
         strategy : TestParameterObjectType.getDefault()])
    public static final OptionalComponentType DISABLED = new OptionalComponentType('disabled', 'DISABLED', [:])

    public static final all = [ENABLED, DISABLED]

    protected static Map types = [:]

    static {
        OptionalComponentType.all.each {
            OptionalComponentType.types[ it.toString() ] = it
        }
    }

    private OptionalComponentType(String displayName, String typeName, Map parameters) {
        super(displayName, typeName, parameters)
    }

    public static OptionalComponentType valueOf(String type) {
        types[type]
    }

    List<IParameterObjectClassifier> getClassifiers() {
       all
    }

    IParameterObject getParameterObject(Map parameters) {
        getStrategy(this, parameters)
    }

    static IParameterObject getStrategy(OptionalComponentType type, Map parameters) {
        switch (type) {
            case OptionalComponentType.ENABLED:
                return new EnabledOptionalComponentStrategy(timeMode: (TimeMode) parameters['timeMode'], strategy : parameters['strategy'])
            case OptionalComponentType.DISABLED:
                return new DisabledOptionalComponentStrategy()
        }
    }
}
