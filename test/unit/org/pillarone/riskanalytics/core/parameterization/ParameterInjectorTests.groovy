package org.pillarone.riskanalytics.core.parameterization

import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier

class ParameterInjectorTests extends GroovyTestCase {

    void testInjectParameterization() {
        CoreModel model = new CoreModel()
        model.initComponents()

        model.exampleInputOutputComponent.parmParameterObject = null

        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreParameters")
        injector.injectConfiguration(model)


        assertNotNull model.exampleInputOutputComponent.parmParameterObject
        assert model.exampleInputOutputComponent.parmParameterObject instanceof IParameterObject
    }

    void testLoadParameters() {
        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreParameters")
        assertNotNull injector.configObject
    }

    void testMultiplePeriodParameterization() {
        Model model = new CoreModel()
        model.initComponents()
        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreMultiPeriodParameters")
        ConfigObject parameters = injector.configObject
        assertNotNull parameters
        assertTrue parameters.components.exampleInputOutputComponent.parmParameterObject[0] instanceof ExampleParameterObject
        assertNotSame parameters.components.exampleInputOutputComponent.parmParameterObject[0], parameters.components.exampleInputOutputComponent.parmParameterObject[1]
        injector.injectConfigToModel(parameters, model, 0)
        assertSame("generator does not match for period 0", parameters.components.exampleInputOutputComponent.parmParameterObject[0], model.exampleInputOutputComponent.parmParameterObject)
        injector.injectConfigToModel(parameters, model, 1)
        assertSame("generator does not match for period 1", parameters.components.exampleInputOutputComponent.parmParameterObject[1], model.exampleInputOutputComponent.parmParameterObject)
    }

    void testIncompleteParametersForPeriodCount() {
        Model model = new CoreModel()
        model.initComponents()
        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreParameters")
        ConfigObject parameters = injector.configObject
        injector.injectConfigToModel(parameters, model, injector.periodCount + 1)
        assertNull("Missing configuration sets parameters to 'null'", model.exampleInputOutputComponent.parmParameterObject)
    }


    void testUnkownParameterFile() {
        shouldFail(FileNotFoundException, {
            new ParameterInjector("src/java/models/UnkownParams")
        })
    }

    void testPeriodCount() {
        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreMultiPeriodParameters")
        assertEquals 2, injector.periodCount
    }

    void testModelCheck() {
        CoreModel model = new CoreModel()
        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreParameters")

        ConfigObject parameter = new ConfigObject()
        // no model in parameter file
        shouldFail(IllegalArgumentException, {
            injector.checkModelMatch(parameter, model)
        })
        // wrong model in parameter file
        parameter.model = Model
        shouldFail(IllegalArgumentException, {
            injector.checkModelMatch(parameter, model)
        })
        // correct model in parameter file
        parameter.model = CoreModel
        injector.checkModelMatch(parameter, model)
    }

    //TODO: fix with new model
    /*void testRecursiveComponents() {
        ParameterInjector injector = new ParameterInjector("src/java/models/sparrow/SparrowParameters")
        ConfigObject config = new ConfigObject()
        config.components.mtpl.subClaimsGenerator.subSingleClaimsGenerator.subFrequencyGenerator.parmDistribution[0] =
            RandomDistributionFactory.getDistribution(FrequencyDistributionType.CONSTANT, ["constant": 1])
        config.components.mtpl.subClaimsGenerator.subSingleClaimsGenerator.subFrequencyGenerator.parmDistribution[1] =
            RandomDistributionFactory.getDistribution(FrequencyDistributionType.CONSTANT, ["constant": 1])
        ExampleCompanyModel model = new ExampleCompanyModel()
        model.init()
        model.mtpl.subClaimsGenerator.subSingleClaimsGenerator.subFrequencyGenerator.parmDistribution = null
        injector.injectConfigToModel(config, model)
        assertNotNull model.mtpl.subClaimsGenerator.subSingleClaimsGenerator.subFrequencyGenerator.parmDistribution

    }*/

    void testDynamicNames() {
        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreMultiPeriodParameters")
        CoreModel model = new CoreModel()
        model.init()
        injector.injectConfigToModel(injector.configObject, model)
        assertNotNull model.dynamicComponent.subSubcomponent.parmParameterObject
    }



    void testMultiPeriodsDynamicComponents() {
        ParameterInjector injector = new ParameterInjector("src/java/models/core/CoreMultiPeriodParameters")
        Model model = new CoreModel()
        model.init()
        model.injectComponentNames()

        injector.injectConfiguration(model, 0)
        assertEquals 1, model.dynamicComponent.subComponentCount()
        def subComponent = model.dynamicComponent.subSubcomponent
        assertNotNull subComponent.parmParameterObject
        assertEquals ExampleParameterObjectClassifier.TYPE0, model.dynamicComponent.subSubcomponent.parmParameterObject.classifier

        injector.injectConfiguration(model, 1)
        assertEquals 1, model.dynamicComponent.subComponentCount()
        assertNotNull subComponent.parmParameterObject
        assertEquals ExampleParameterObjectClassifier.TYPE1, model.dynamicComponent.subSubcomponent.parmParameterObject.classifier


        assertSame subComponent, model.dynamicComponent.subSubcomponent
    }

}