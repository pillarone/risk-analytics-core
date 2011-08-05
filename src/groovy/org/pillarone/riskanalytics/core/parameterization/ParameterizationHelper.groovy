package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.EnumTagType

public class ParameterizationHelper {

    static Parameterization createDefaultParameterization(Model model, int periodCount = 1) {
        Parameterization result = new Parameterization(model.class.simpleName - "Model" + "-Default")
        result.modelClass = model.class
        periodCount.times {index ->
            model.init()
            List parameterList = extractParameterHoldersFromModel(model, index)
            parameterList.each {
                result.addParameter(it)
            }
        }
        result.periodCount = periodCount
        return result
    }

    static List<ParameterHolder> extractParameterHoldersFromModel(Model model, int periodIndex) {
        return getAllParameter(model).collect {Map.Entry entry -> ParameterHolderFactory.getHolder(entry.key, periodIndex, entry.value) }
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
        result.periodCount = injector.periodCount

        addParameters(result, injector, model)

        addPeriodLabels(result, configObject)

        addComments(result, configObject)

        addTags(result, configObject)
        return result
    }

    static void addParameters(Parameterization result, ParameterInjector injector, Model model) {
        injector.periodCount.times {index ->
            injector.injectConfiguration(model, index)
            Map parameterMap = [:]
            collectAllParameter(model, parameterMap)
            List parameterList = parameterMap.collect {Map.Entry entry -> ParameterHolderFactory.getHolder(entry.key, index, entry.value) }
            parameterList.each {
                result.addParameter(it)
            }
        }
    }

    static void addPeriodLabels(Parameterization result, ConfigObject configObject) {
        def periodLabels = []
        if (configObject.containsKey("periodLabels")) {
            periodLabels = configObject.periodLabels as String[]
        }
        result.periodLabels = periodLabels
    }

    static void addComments(Parameterization result, ConfigObject configObject) {
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
    }

    static void addTags(Parameterization result, ConfigObject configObject) {
        //tags
        if (configObject.containsKey("tags")) {
            List tagNames = configObject.tags as List
            List tags = []
            tagNames.each {String name ->
                Tag tag = Tag.findByNameAndTagType(name, EnumTagType.PARAMETERIZATION)
                if (!tag) tag = new Tag(name: name, tagType: EnumTagType.PARAMETERIZATION).save()
                tags << tag
            }
            result.setTags(tags as Set)
        }
    }

    protected static Map getAllParameter(Model model) {
        def parameter = [:]
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