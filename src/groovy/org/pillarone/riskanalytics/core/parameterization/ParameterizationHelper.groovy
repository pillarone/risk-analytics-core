package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment

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
        //comments
        if (configObject.containsKey("comments")) {
            List comments = configObject.comments as List
            if (!comments.isEmpty()) {
                GroovyShell shell = new GroovyShell(ParameterizationHelper.class.getClassLoader())
                comments.each {
                    result.addComment(new Comment((Map) shell.evaluate(it)))
                }
            }
        }

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

    static List copyParameters(List parameters) {
        return parameters.collect { it.clone() }
    }
}