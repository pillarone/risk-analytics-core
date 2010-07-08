package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.parameter.*

class ParameterHolderFactory {

    public static ParameterHolder getHolder(String path, int periodIndex, int value) {
        return new IntegerParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(String path, int periodIndex, double value) {
        return new DoubleParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(String path, int periodIndex, boolean value) {
        return new BooleanParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(String path, int periodIndex, String value) {
        return new StringParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(String path, int periodIndex, DateTime value) {
        return new DateParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(String path, int periodIndex, ConstrainedString value) {
        return new ConstrainedStringParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(String path, int periodIndex, Enum value) {
        return new EnumParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(String path, int periodIndex, IParameterObject value) {
        return new ParameterObjectParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(String path, int periodIndex, AbstractMultiDimensionalParameter value) {
        return new MultiDimensionalParameterHolder(path, periodIndex, value)
    }

    public static ParameterHolder getHolder(Parameter parameter) {
        switch (parameter.persistedClass()) {
            case IntegerParameter:
                return createIntegerHolder(parameter)
            case DoubleParameter:
                return createDoubleHolder(parameter)
            case BooleanParameter:
                return createBooleanHolder(parameter)
            case StringParameter:
                return createStringHolder(parameter)
            case ConstrainedStringParameter:
                return createConstrainedStringHolder(parameter)
            case EnumParameter:
                return createEnumHolder(parameter)
            case ParameterObjectParameter:
                return createParamaterObjectHolder(parameter)
            case MultiDimensionalParameter:
                return createMultiDimensionalParameterHolder(parameter)
            case DateParameter:
                return createDateHolder(parameter)
            default:
                throw new RuntimeException("Unknown paramter type: ${parameter.class}")
        }
    }


    private static ParameterHolder createIntegerHolder(Parameter parameter) {
        return new IntegerParameterHolder(parameter)
    }

    private static ParameterHolder createDoubleHolder(Parameter parameter) {
        return new DoubleParameterHolder(parameter)
    }

    private static ParameterHolder createBooleanHolder(Parameter parameter) {
        return new BooleanParameterHolder(parameter)
    }

    private static ParameterHolder createStringHolder(Parameter parameter) {
        return new StringParameterHolder(parameter)
    }

    private static ParameterHolder createConstrainedStringHolder(Parameter parameter) {
        return new ConstrainedStringParameterHolder(parameter)
    }

    private static ParameterHolder createDateHolder(Parameter parameter) {
        return new DateParameterHolder(parameter)
    }

    private static ParameterHolder createEnumHolder(Parameter parameter) {
        return new EnumParameterHolder(parameter)
    }

    private static ParameterHolder createParamaterObjectHolder(Parameter parameter) {
        return new ParameterObjectParameterHolder(parameter)
    }

    private static ParameterHolder createMultiDimensionalParameterHolder(Parameter parameter) {
        return new MultiDimensionalParameterHolder(parameter)
    }

    /**
     * Removes all parameters whose path starts with oldPath and adds copies of the old parameters
     * to the parameterization with the path replaced with newPath.
     * This can be used to rename all parameters of a component inclusive all of their sub component parameters.
     */
    public static void renamePathOfParameter(Parameterization parameterization, String oldPath, String newPath) {
        List removedParameters = []
        List clonedParameters = []
        parameterization.parameters.each {ParameterHolder parameterHolder ->
            if (parameterHolder.path.startsWith(oldPath + ":")) {
                ParameterHolder cloned = parameterHolder.clone()
                cloned.path = cloned.path.replace("${oldPath}", "${newPath}")
                removedParameters << parameterHolder
                clonedParameters << cloned
            }
        }
        removedParameters.each {ParameterHolder parameterHolder ->
            parameterization.removeParameter parameterHolder
        }
        clonedParameters.each {ParameterHolder parameterHolder ->
            parameterization.addParameter parameterHolder
        }
    }

    public static void duplicateParameters(Parameterization parameterization, String oldPath, String newPath) {
        List clonedParameters = []
        parameterization.parameters.each {ParameterHolder parameterHolder ->
            if (parameterHolder.path.startsWith(oldPath + ":")) {
                ParameterHolder cloned = parameterHolder.clone()
                cloned.path = cloned.path.replace("${oldPath}", "${newPath}")
                clonedParameters << cloned
            }
        }
        clonedParameters.each {ParameterHolder parameterHolder ->
            parameterization.addParameter parameterHolder
        }
    }

}
