package org.pillarone.riskanalytics.core.parameterization

import models.core.CoreModel
import models.core.parameterApplicator.ParameterApplicatorModel
import org.apache.commons.logging.LogFactory
import org.junit.Test
import org.pillarone.riskanalytics.core.example.component.ExampleDynamicComponent
import org.pillarone.riskanalytics.core.example.component.ExampleInputOutputComponent
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

class ParameterApplicatorTests {


    private Parameterization getParameterization(File paramFile) {
        ConfigObject params = new ConfigSlurper().parse(paramFile.toURL())
        Parameterization parameterization = ParameterizationHelper.createParameterizationFromConfigObject(params, "dummy")
        parameterization.save()
        return parameterization
    }

    @Test
    void testCreateApplicableParameter() {

        Model m = new CoreModel()
        m.init()

        String path = "exampleInputOutputComponent:parmParameterObject"
        def value = ExampleParameterObjectClassifier.TYPE0.getParameterObject(ExampleParameterObjectClassifier.TYPE0.parameters)

        ParameterApplicator applicator = new ParameterApplicator()
        ApplicableParameter parameter = applicator.createApplicableParameter(m, ParameterHolderFactory.getHolder(path, 0, value))

        assertSame m.exampleInputOutputComponent, parameter.component
        assertEquals "parmParameterObject", parameter.parameterPropertyName
        assertSame value.type, parameter.parameterValue.type
        assertEquals value.parameters, parameter.parameterValue.parameters

    }

    @Test
    void testBuildApplicableParameter() {
        Parameterization parameter = getParameterization(new File("src/java/models/core/CoreParameters.groovy"))

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

    @Test
    void testInit() {
        Parameterization parameter = getParameterization(new File("src/java/models/core/CoreParameters.groovy"))

        Model m = new CoreModel()
        m.init()

        ParameterApplicator applicator = new ParameterApplicator(model: m, parameterization: parameter)

        assertNull applicator.parameterPerPeriod

        applicator.init()

        assertNotNull applicator.parameterPerPeriod
    }

    @Test
    void testInitWithNestedMdp() {
        Parameterization parameter = getParameterization(new File("src/java/models/core/CoreParameters.groovy"))
        ParameterObjectParameterHolder param = parameter.parameterHolders.find { it.path = "exampleInputOutputComponent:parmParameterObject" }
        param.setValue(ExampleParameterObjectClassifier.NESTED_MDP.toString())
        param.clearCachedValues()
        Model m = new CoreModel()
        m.init()

        ParameterApplicator applicator = new ParameterApplicator(model: m, parameterization: parameter)

        assertNull applicator.parameterPerPeriod

        applicator.init()

        assertNotNull applicator.parameterPerPeriod
        ApplicableParameter mdpObject = applicator.parameterPerPeriod[0].find { it.parameterValue.parameters.keySet().contains("mdp") }
        assertNotNull mdpObject
        AbstractMultiDimensionalParameter mdp = mdpObject.parameterValue.parameters.get("mdp")
        assertNotNull mdp
        assertNotNull mdp.simulationModel
    }

    //TODO msp: create better test model

    @Test
    void testApplyParameter() {

        Parameterization parameter = getParameterization(new File("src/java/models/core/CoreParameters.groovy"))

        Model m = new CoreModel()
        m.init()

        assertSame "original value", ExampleParameterObjectClassifier.TYPE0, m.exampleInputOutputComponent.parmParameterObject.type

        ParameterApplicator applicator = new ParameterApplicator(model: m, parameterization: parameter)
        applicator.init()

        applicator.applyParameterForPeriod(0)

        assertSame "parameterized value", ExampleParameterObjectClassifier.TYPE0, m.exampleInputOutputComponent.parmParameterObject.type

    }

    @Test
    void testDynamicComposedComponentExpansion() {

        Parameterization parameter = getParameterization(new File("src/java/models/core/CoreParameters.groovy"))

        CoreModel m = new CoreModel()
        m.init()

        assertEquals "# dynamic lobs before expansion", 0, m.dynamicComponent.subComponentCount()

        ParameterApplicator applicator = new ParameterApplicator(model: m, parameterization: parameter)
        applicator.init()

        assertEquals "# dynamic lobs after expansion", 1, m.dynamicComponent.subComponentCount()
    }



    @Test
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

    @Test
    void testAbstractMultiDimensionalParameterHandling() {
        Parameterization parameter = getParameterization(new File("src/java/models/core/parameterApplicator/ParameterApplicatorParameters.groovy"))

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

    @Test
    void testConstraindedStringHandling() {
        Parameterization parameter = getParameterization(new File("src/java/models/core/parameterApplicator/ParameterApplicatorParameters.groovy"))

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

    @Test
    void testParameterizationPerformance() {
        Parameterization parameter = getParameterization(new File("src/java/models/core/CoreParameters.groovy"))

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