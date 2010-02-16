package org.pillarone.riskanalytics.core.parameterization

import models.core.CoreModel
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.IntegerParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.StringParameterHolder

class ParameterizationHelperTests extends GroovyTestCase {

    void testDefaultName() {
        CoreModel model = new CoreModel()
        Parameterization parameterization = ParameterizationHelper.createDefaultParameterization(model)
        assertEquals "Core-Default", parameterization.name
    }

    void testModelClassName() {
        CoreModel model = new CoreModel()
        Parameterization parameterization = ParameterizationHelper.createDefaultParameterization(model)
        assertSame CoreModel, parameterization.modelClass
    }

    void testGetAllParameter() {
        CoreModel model = new CoreModel()

        Map parameter = ParameterizationHelper.getAllParameter(model)

        assertEquals 1, parameter.size()

        assertSame model.exampleInputOutputComponent.parmParameterObject, parameter["exampleInputOutputComponent:parmParameterObject"]
    }

    void testCreateDefaultParameterization() {
        int initialParameterCount = Parameter.count()
        CoreModel model = new CoreModel()
        Parameterization parameterization = ParameterizationHelper.createDefaultParameterization(model)
        assertEquals 1, parameterization.parameters.size()

        parameterization.save()
        assertEquals initialParameterCount + 4, Parameter.count()
    }

    void testCreateDefaultParameterizationForMultiplePeriods() {
        int initialParameterCount = Parameter.count()
        CoreModel model = new CoreModel()
        Parameterization parameterization = ParameterizationHelper.createDefaultParameterization(model, 3)
        assertEquals 3, parameterization.parameters.size()

        parameterization.save()
        assertEquals initialParameterCount + 12, Parameter.count()
    }

    void testCreateParameterizationFromConfigObject() {
        ConfigObject configObject = new ConfigObject()
        configObject.model = CoreModel
        configObject.periodCount = 2
        configObject.displayName = 'Name'
        configObject.components.exampleInputOutputComponent.parmParameterObject[0] = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0, "b": 1])
        configObject.components.exampleInputOutputComponent.parmParameterObject[1] = ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0, "p2": 1])

        Parameterization param = ParameterizationHelper.createParameterizationFromConfigObject(configObject, 'anotherName')
        param.save()
        assertEquals 'Name', param.name

        def parameterObject = param.getParameters('exampleInputOutputComponent:parmParameterObject')
        assertEquals 2, parameterObject.size()

        def pop = parameterObject[0] as ParameterObjectParameterHolder
        assertEquals 'TYPE0', pop.classifier.toString()

        pop = parameterObject[1] as ParameterObjectParameterHolder
        assertEquals 'TYPE1', pop.classifier.toString()
    }

    void testCopyParameters() {
        List params = []
        IntegerParameterHolder p1 = ParameterHolderFactory.getHolder('intPath', 2, 5)
        params << p1
        StringParameterHolder p2 = ParameterHolderFactory.getHolder('strPath', 1, "test")
        params << p2

        def newParams = ParameterizationHelper.copyParameters(params)

        assertEquals params.size(), newParams.size()
        def newP1 = newParams.find { it.path == 'intPath' }
        assertNotNull newP1
        assertEquals p1.periodIndex, newP1.periodIndex
        assertEquals p1.businessObject, newP1.businessObject

        def newP2 = newParams.find { it.path == 'strPath' }
        assertNotNull newP2
        assertEquals p2.periodIndex, newP2.periodIndex
        assertEquals p2.businessObject, newP2.businessObject
    }

}