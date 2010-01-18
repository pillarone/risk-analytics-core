package org.pillarone.riskanalytics.core.parameterization

import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObject
import org.pillarone.riskanalytics.core.model.Model

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

    //TODO: fix with new model
    /*void testInjectionOfModelIntoMultiDimensionalParams() {
        ParameterInjector injector = new ParameterInjector("src/java/models/capitalEagle/CapitalEagleParameters")
        Model model = new CapitalEagleModel()
        model.init()
        injector.injectConfiguration(model)
        assertEquals model, model.mtpl.subUnderwriting.parmUnderwritingInformation.simulationModel
    }
    void testInjectionOfModelIntoMultiDimensionalParamsInsideParameterObjects() {
        ParameterInjector injector = new ParameterInjector("src/java/models/capitalEagle/CapitalEagleAttritionalDependencies2Parameters")
        Model model = new CapitalEagleAttritionalDependenciesModel()
        model.init()
        injector.injectConfiguration(model)
        model.copulaAttritional.parmCopulaStrategy.parameters.values().each {
            if (it instanceof AbstractMultiDimensionalParameter) {
                assertEquals model, it.simulationModel
            }
        }
    }

    void testInjectionOfModelIntoConstrainedStrings() {
        ParameterInjector injector = new ParameterInjector("src/java/models/dependency/DependencyParameters")
        Model model = new DependencyModel()
        model.init()
        model.injectComponentNames()
        injector.injectConfiguration(model)
        assertNotNull model.fire.subSeverityExtractor.parmFilterCriteria.selectedComponent
        assertEquals 'fire', model.fire.subSeverityExtractor.parmFilterCriteria.selectedComponent.name
    }

    void testMultiPeriodsDynamicComponents() {
        ParameterInjector injector = new ParameterInjector("src/java/models/asset/AssetParameters")
        Model model = new AssetModel()
        model.init()
        model.injectComponentNames()

        injector.injectConfiguration(model, 0)
        assertEquals 2, model.bonds.subComponentCount()
        def subSwiss = model.bonds.subSwiss
        assertNotNull subSwiss.parmQuantity
        assertEquals 2000, model.bonds.subSwiss.parmQuantity

        injector.injectConfiguration(model, 1)
        assertEquals 2, model.bonds.subComponentCount()
        assertNotNull model.bonds.subSwiss.parmQuantity
        assertEquals 0, model.bonds.subSwiss.parmQuantity

        assertSame subSwiss, model.bonds.subSwiss
    }*/

}