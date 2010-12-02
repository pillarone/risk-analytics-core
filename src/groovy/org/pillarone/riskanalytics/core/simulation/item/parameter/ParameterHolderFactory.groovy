package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.parameter.*
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.IComponentMarker
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.components.ComponentUtils

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
     * Furthermore all parameters referencing it are renamed accordingly.
     */
    public static List<String> renamePathOfParameter(Parameterization parameterization, String oldPath, String newPath) {
        List removedParameters = []
        List clonedParameters = []
        parameterization.parameters.each {ParameterHolder parameterHolder ->
            if (parameterHolder.path.startsWith(oldPath + ":")) {
                renamePathOfParameter(parameterHolder, removedParameters, clonedParameters, oldPath, newPath, false)
            }
        }
        removedParameters.each {ParameterHolder parameterHolder ->
            parameterization.removeParameter parameterHolder
        }
        clonedParameters.each {ParameterHolder parameterHolder ->
            parameterization.addParameter parameterHolder
        }
        return renameReferencingParameters(parameterization, oldPath, newPath)
    }

    private static ParameterHolder renamePathOfParameter(ParameterHolder parameterHolder, List<ParameterHolder> removedParameters,
                                              List<ParameterHolder> clonedParameters, String oldPath, String newPath, boolean isNested) {
        ParameterHolder cloned = parameterHolder.clone()
        cloned.path = cloned.path.replace(oldPath, newPath)
        if (!isNested) {
            removedParameters << parameterHolder
            clonedParameters << cloned
        }
        if (cloned instanceof ParameterObjectParameterHolder) {
            // recursive step down
            cloned.getClassifierParameters().clear()
            for (Map.Entry<String, ParameterHolder> nestedParameterHolder : ((ParameterObjectParameterHolder) parameterHolder).getClassifierParameters().entrySet()) {
                ParameterHolder clonedNested = renamePathOfParameter(nestedParameterHolder.value, removedParameters, clonedParameters, oldPath, newPath, true)
                cloned.getClassifierParameters().putAt(nestedParameterHolder.key, clonedNested)
            }
        }
        return cloned
    }

    /**
     * @param parameterization
     * @param oldComponentPath
     * @param newComponentPath
     * @return all referencing components being renamed from oldComponentPath to newComponentPath using the marker
     *          interface of the component @ oldComponentPath
     */
    private static List<String> renameReferencingParameters(Parameterization parameterization, String oldComponentPath, String newComponentPath) {
        String oldComponentName = ComponentUtils.getComponentNormalizedName(oldComponentPath)
        String newComponentName = ComponentUtils.getComponentNormalizedName(newComponentPath)
        Class markerInterface = getMarkerInterface(parameterization, oldComponentPath)
        List<ParameterHolder> markerParameterHolders = affectedParameterHolders(parameterization, markerInterface, oldComponentName)
        List<String> referencingPaths = []
        for (ParameterHolder parameterHolder: markerParameterHolders) {
            if (!parameterHolder.removed) {
                List<String> paths = parameterHolder.updateReferenceValues(markerInterface, oldComponentName, newComponentName)
                if (paths.size() > 0) {
                    referencingPaths.addAll paths
                }
            }
        }
        return referencingPaths
    }

    /**
     * @param parameterization
     * @param componentPath
     * @return model path of all parameters referencing the component using its marker interface
     */
    public static List<String> referencingParametersPaths(Parameterization parameterization, String componentPath) {
        String componentName = ComponentUtils.getComponentNormalizedName(componentPath)
        Class markerInterface = getMarkerInterface(parameterization, componentPath)
        List<ParameterHolder> markerParameterHolders = affectedParameterHolders(parameterization, markerInterface, componentPath)
        List<String> referencingPaths = []
        for (ParameterHolder parameterHolder : markerParameterHolders) {
            if (!parameterHolder.removed) {
                List<String> paths = parameterHolder.referencePaths(markerInterface, componentName)
                if (paths.size() > 0) {
                    referencingPaths.addAll paths
                }
            }
        }
        return referencingPaths
    }

    private static List<ParameterHolder> affectedParameterHolders(Parameterization parameterization, Class markerInterface, String componentPath) {
        List<ParameterHolder> referencedParameterHolders = new ArrayList<ParameterHolder>()
        if (markerInterface) {
            for (ParameterHolder parameterHolder: parameterization.parameterHolders) {
                if (parameterHolder instanceof IMarkerValueAccessor) {
                    referencedParameterHolders.add parameterHolder
                }
            }
        }
        return referencedParameterHolders
    }

    /**
     * @param parameterization
     * @param path of component
     * @return class of the marker interface the component @path is implementing or <tt>null</tt> if not found
     */
    private static Class getMarkerInterface(Parameterization parameterization, String path) {
        Model model = (Model) parameterization.modelClass.newInstance()
        // fill model.allComponents
        model.init()
        // init component.name
        model.injectComponentNames()
        // find the component class of the component @path
        Class componentClassToBeRenamed
        for (Component component: model.allComponents) {
            if (component instanceof DynamicComposedComponent && path.contains(component.name)) {
                componentClassToBeRenamed = component.createDefaultSubComponent().class
                break
            }
        }
        if (componentClassToBeRenamed) {
            // search the marker interface
            for (Class intf: componentClassToBeRenamed.interfaces) {
                if (IComponentMarker.isAssignableFrom(intf)) {
                    return intf
                }
            }
        }
        return null
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
