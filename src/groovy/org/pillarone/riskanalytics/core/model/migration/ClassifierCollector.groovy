package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.model.ModelPath
import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject

class ClassifierCollector implements IModelVisitor {

    private Class classifierClass
    private String name

    List<ResultPair> result = []

    ClassifierCollector(Class classifier, String name) {
        this.classifierClass = classifier
        this.name = name
    }

    void visitComponent(Component component, ModelPath path) {
    }

    void visitModel(Model model) {

    }

    void visitParameterObject(IParameterObject parameterObject, ModelPath path) {
        if (parameterObject.type.class.name == classifierClass.name) {
            AbstractParameterObjectClassifier parameterObjectClassifier = parameterObject.type
            if (parameterObjectClassifier.typeName == name) {
                result << new ResultPair(path: path, classifierParameters: parameterObject.parameters)
            }
        }
    }

    private static class ResultPair {
        ModelPath path
        Map classifierParameters
    }

}
