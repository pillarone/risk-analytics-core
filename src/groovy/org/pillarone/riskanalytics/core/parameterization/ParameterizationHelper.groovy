package org.pillarone.riskanalytics.core.parameterization

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.parameter.ConstrainedStringParameter
import org.pillarone.riskanalytics.core.parameter.DateParameter
import org.pillarone.riskanalytics.core.parameter.DoubleParameter
import org.pillarone.riskanalytics.core.parameter.EnumParameter
import org.pillarone.riskanalytics.core.parameter.IntegerParameter
import org.pillarone.riskanalytics.core.parameter.MultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ParameterObjectParameter
import org.pillarone.riskanalytics.core.parameter.StringParameter
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

public class ParameterizationHelper {

    static Parameterization createDefaultParameterization(Model model, int periodCount = 1) {
        Parameterization result = new Parameterization(model.class.simpleName - "Model" + "-Default")
        result.modelClass = model.class
        Map parameterMap = getAllParameter(model)
        periodCount.times {index ->
            List parameterList = parameterMap.collect {Map.Entry entry -> ParameterHolderFactory.getHolder(entry.key, index, entry.value) }
            parameterList.each {
                result.addParameter(it)
            }
        }
        result.periodCount = periodCount
        return result
    }

    static Parameterization createParameterizationFromConfigObject(ConfigObject configObject, String paramName) {
        Model model = configObject.model.newInstance()
        model.init()

        FileImportService.spreadRanges(configObject)

        ParameterInjector injector = new ParameterInjector(configObject)
        if (configObject.containsKey('displayName')) {
            paramName = configObject.displayName
        }
        Parameterization result = new Parameterization(paramName)
        result.modelClass = model.class

        injector.periodCount.times {index ->
            injector.injectConfiguration(model, index)
            Map parameterMap = [:]
            collectAllParameter(model, parameterMap)
            List parameterList = parameterMap.collect {Map.Entry entry -> ParameterHolderFactory.getHolder(entry.key, index, entry.value) }
            parameterList.each {
                result.addParameter(it)
            }
        }

        result.periodCount = injector.periodCount
        def periodLabels = []
        if (configObject.containsKey("periodLabels")) {
            periodLabels = configObject.periodLabels as String[]
        }
        result.periodLabels = periodLabels
        return result
    }

    protected static Map getAllParameter(Model model) {
        def parameter = [:]
        model.init()
        collectAllParameter(model, parameter)
        return parameter
    }

    protected static void collectAllParameter(Model model, Map parameter) {
        model.properties.each {propertyName, propertyValue ->
            if (propertyValue instanceof Component) {
                collectAllParameter propertyValue, propertyName, parameter
            }
        }
    }

    protected static void collectAllParameter(Component component, String prefix, Map parameter) {

        component.properties.each {propertyName, propertyValue ->
            if (propertyName.startsWith("parm")) {
                parameter[prefix + ":" + propertyName] = propertyValue
            }
            if (propertyName.startsWith("sub")) {
                collectAllParameter(propertyValue, prefix + ":" + propertyName, parameter)
            }
        }
    }

    static List getParameter(Map parameterMap, int periodCount = 1) {
        List result = []
        periodCount.times {i ->
            result.addAll(parameterMap.collect {entry ->
                def parameter = getParameter(entry.key, entry.value)
                parameter.periodIndex = i
                return parameter
            })
        }
        return result
    }

    private static List getParameterForPeriodIndex(Map parameterMap, int periodIndex) {
        List result = []
        result.addAll(parameterMap.collect {entry ->
            def parameter = getParameter(entry.key, entry.value)
            parameter.periodIndex = periodIndex
            return parameter
        })
        return result
    }

    public static Parameter getParameter(String path, def value) {
        if (value == null) {
            throw new IllegalArgumentException("null parameter for: $path")
        }
        if (value.class.isEnum()) {
            return new EnumParameter(path: path, parameterInstance: value)
        }
        throw new IllegalArgumentException("unkown parameter type: ${value.class}")
    }

    public static Parameter getParameter(String path, String value) {
        return new StringParameter(path: path, parameterInstance: value)
    }

    public static Parameter getParameter(String path, ConstrainedString value) {
        return new ConstrainedStringParameter(path: path, parameterInstance: value)
    }

    public static Parameter getParameter(String path, Integer value) {
        return new IntegerParameter(path: path, parameterInstance: value)
    }

    public static Parameter getParameter(String path, Double value) {
        return new DoubleParameter(path: path, parameterInstance: value)
    }

    public static Parameter getParameter(String path, BigDecimal value) {
        return new DoubleParameter(path: path, parameterInstance: value.toDouble())
    }

    public static Parameter getParameter(String path, DateTime value) {
        DateParameter parameter = new DateParameter(path: path)
        parameter.parameterInstance = value
        return parameter
    }

    public static Parameter getParameter(String path, Date value) {
        DateParameter parameter = new DateParameter(path: path)
        parameter.parameterInstance = new DateTime(value.time)
        return parameter
    }

    public static Parameter getParameter(String path, IParameterObject value) {
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: path)
        parameter.parameterInstance = value
        return parameter
    }

    public static Parameter getParameter(String path, AbstractMultiDimensionalParameter value) {
        MultiDimensionalParameter parameter = new MultiDimensionalParameter(path: path)
        parameter.parameterInstance = value
        return parameter
    }

    static List copyParameters(List parameters) {
        return parameters.collect { it.clone() }
    }
}