package org.pillarone.riskanalytics.core.parameterization

import grails.test.GrailsUnitTestCase
import models.core.CoreModel
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.example.component.ExampleDynamicComponent
import org.pillarone.riskanalytics.core.example.component.ExampleInputOutputComponent
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import models.core.parameterApplicator.ParameterApplicatorModel

class ParameterApplicatorTests extends GrailsUnitTestCase {


    private ParameterizationDAO getParamDAO(File paramFile) {
        ConfigObject params = new ConfigSlurper().parse(paramFile.toURL())
        Parameterization parameterization = ParameterizationHelper.createParameterizationFromConfigObject(params, "dummy")
        parameterization.save()
        def parameter = parameterization.dao
        parameter.periodCount = params.periodCount
        return parameter
    }

    void testCreateApplicableParameter() {

        Model m = new CoreModel()
        m.init()

        String path = "exampleInputOutputComponent:parmParameterObject"
        def value = ExampleParameterObjectClassifier.TYPE0.getParameterObject(ExampleParameterObjectClassifier.TYPE0.parameters)

        ParameterApplicator applicator = new ParameterApplicator()
        ApplicableParameter parameter = applicator.createApplicableParameter(m, path, value)

        assertSame m.exampleInputOutputComponent, parameter.component
        assertEquals "parmParameterObject", parameter.parameterPropertyName
        assertSame value, parameter.parameterValue

    }

    void testBuildApplicableParameter() {
        ParameterizationDAO parameter = getParamDAO(new File("src/java/models/core/CoreParameters.groovy"))

        Model m = new CoreModel()
        m.init()


        ParameterApplicator applicator = new ParameterApplicator(model: m)

        List applicableParameter = applicator.buildApplicableParameter(parameter)
        assertNotNull applicableParameter
        assertEquals "periods", parameter.periodCount, applicableParameter.size()
        applicableParameter.each {List parameterForPeriod ->
            assertNotNull parameterForPeriod
            assertFalse "empty parameter for period", parameterForPeriod.empty
        }
    }

    void testInit() {
        ParameterizationDAO parameter = getParamDAO(new File("src/java/models/core/CoreParameters.groovy"))

        Model m = new CoreModel()
        m.init()

        ParameterApplicator applicator = new ParameterApplicator(model: m, parameterization: parameter)

        assertNull applicator.parameterPerPeriod

        applicator.init()

        assertNotNull applicator.parameterPerPeriod
    }

    //TODO msp: create better test model
    void testApplyParameter() {

        ParameterizationDAO parameter = getParamDAO(new File("src/java/models/core/CoreParameters.groovy"))

        Model m = new CoreModel()
        m.init()

        assertSame "original value", ExampleParameterObjectClassifier.TYPE0, m.exampleInputOutputComponent.parmParameterObject.type

        ParameterApplicator applicator = new ParameterApplicator(model: m, parameterization: parameter)
        applicator.init()

        applicator.applyParameterForPeriod(0)

        assertSame "parameterized value", ExampleParameterObjectClassifier.TYPE0, m.exampleInputOutputComponent.parmParameterObject.type

    }

    void testDynamicComposedComponentExpansion() {

        ParameterizationDAO parameter = getParamDAO(new File("src/java/models/core/CoreParameters.groovy"))

        CoreModel m = new CoreModel()
        m.init()

        assertEquals "# dynamic lobs before expansion", 0, m.dynamicComponent.subComponentCount()

        ParameterApplicator applicator = new ParameterApplicator(model: m, parameterization: parameter)
        applicator.init()

        assertEquals "# dynamic lobs after expansion", 1, m.dynamicComponent.subComponentCount()
    }



    void testGetPropertyOrSubComponent() {
        ParameterApplicator applicator = new ParameterApplicator()
        ExampleInputOutputComponent component = new ExampleInputOutputComponent()
        assertEquals component.parmParameterObject, applicator.getPropertyOrSubComponent("parmParameterObject", component)

        // TODO (Oct 2, 2009, msh): check why hudson throws different exc
/*
        shouldFail(MissingPropertyException) {
            applicator.getPropertyOrSubComponent("foo", component)
        }
*/

        ExampleDynamicComponent dynamicComposedComponent = new ExampleDynamicComponent()

        def newSubComponent = applicator.getPropertyOrSubComponent("subSomething", dynamicComposedComponent)
        assertNotNull newSubComponent
        assertSame newSubComponent, dynamicComposedComponent.subSomething

        shouldFail(MissingPropertyException) {
            applicator.getPropertyOrSubComponent("unknowPropertyNotStartingWithsub", dynamicComposedComponent)
        }

    }

    void testAbstractMultiDimensionalParameterHandling() {
        ParameterizationDAO parameter = getParamDAO(new File("src/java/models/core/parameterApplicator/ParameterApplicatorParameters.groovy"))

        Model model = new ParameterApplicatorModel()
        model.init()

        ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: parameter)
        applicator.init()

        def multiDimensionalParameters = applicator.parameterPerPeriod[0].findAll {ApplicableParameter p ->
            p.parameterValue instanceof AbstractMultiDimensionalParameter
        }
        assertFalse "no multiDimensionalParameter found in parameterization", multiDimensionalParameters.empty
        multiDimensionalParameters.each {ApplicableParameter p ->
            assertSame "model on parameter ${p.parameterPropertyName}", model, p.parameterValue.simulationModel
        }

    }

    void testConstraindedStringHandling() {
        ParameterizationDAO parameter = getParamDAO(new File("src/java/models/core/parameterApplicator/ParameterApplicatorParameters.groovy"))

        Model model = new ParameterApplicatorModel()
        model.init()
        model.injectComponentNames()

        ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: parameter)
        applicator.init()

        def constrainedStrings = applicator.parameterPerPeriod[0].findAll {ApplicableParameter p ->
            p.parameterValue instanceof ConstrainedString
        }
        assertFalse "no ConstrainedString found in parameterization", constrainedStrings.empty
        constrainedStrings.each {ApplicableParameter p ->
            assertNotNull "selectedComponent not set for ${p.parameterPropertyName}", p.parameterValue.selectedComponent
        }
    }

    void testParameterizationPerformance() {
        ParameterizationDAO parameter = getParamDAO(new File("src/java/models/core/CoreParameters.groovy"))

        Model model = new CoreModel()
        model.init()
        long apply = 0
        long init = 0
        100.times {
            long start = System.currentTimeMillis()
            ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: parameter)
            applicator.init()
            long afterInit = System.currentTimeMillis()
            init = init + (afterInit - start)
            applicator.applyParameterForPeriod(0)
            long end = System.currentTimeMillis()
            apply = apply + (end - afterInit)
        }


        LogFactory.getLog(ParameterApplicator).info "ParameterApplicator init in: ${init / 100} ms"
        LogFactory.getLog(ParameterApplicator).info "Parameter for period applied in: ${apply / 100} ms"

    }
}