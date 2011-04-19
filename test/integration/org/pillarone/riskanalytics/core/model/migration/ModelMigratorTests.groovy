package org.pillarone.riskanalytics.core.model.migration

import models.migratableCore.MigratableCoreModel
import models.migratableCore.MigratableCoreModel.Migration_v1_v2
import org.pillarone.riskanalytics.core.fileimport.ModelFileImportService
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.MultiDimensionalParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder

class ModelMigratorTests extends GroovyTestCase {

    String oldParameterization = """
package models.migratableCore

model=models.migratableCore.MigratableCoreModel
periodCount=1
applicationVersion='1.3'
periodLabels=["2011-03-30","2011-03-30","2011-03-30"]
components {
	composite {
		parmStrategy[0]=org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.getStrategy(org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.THREE_COLUMNS, ["table":new org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([["S1", "S2"], ["Motor", "Engine"], [100.0, 80.0]]),["id","type","value"], org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('constrained table')),"mode":org.pillarone.riskanalytics.core.example.migration.ResultViewMode.INCREMENTAL,])
		parmTimeMode[0]=org.pillarone.riskanalytics.core.example.migration.TimeMode.PERIOD
	}
	dynamic {
		subOpt1 {
			parmStrategy[0]=org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.getStrategy(org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.THREE_COLUMNS, ["table":new org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([["S1", "S2"], ["Motor", "Engine"], [100.0, 80.0]]),["id","type","value"], org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('constrained table')), "mode": org.pillarone.riskanalytics.core.example.migration.ResultViewMode.INCREMENTAL,])
			parmTimeMode[0]=org.pillarone.riskanalytics.core.example.migration.TimeMode.PERIOD
		}
	}
	exampleInputOutputComponent {
		parmNewParameterObject[0]=org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier.getStrategy(org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier.TYPE0, ["b":1.0,"a":0.0,])
		parmParameterObject[0]=org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier.getStrategy(org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier.TYPE0, ["b":1.0,"a":0.0,])
	}
}
comments=[]

    """

    ClassLoader modelMigrationClassLoader

    void setUp() {
        new ModelFileImportService().compareFilesAndWriteToDB(["MigratableCore"])
        new ModelStructureImportService().compareFilesAndWriteToDB(["MigratableCore"])
        new ResultConfigurationImportService().compareFilesAndWriteToDB(["MigratableCore"])

        ConfigSlurper slurper = new ConfigSlurper()

        modelMigrationClassLoader = new ModelMigrationClassLoader([new Migration_v1_v2().oldModelJarURL] as URL[], Thread.currentThread().contextClassLoader)
        modelMigrationClassLoader.loadClass("models.migratableCore.MigratableCoreModel")

        slurper.classLoader = new GroovyClassLoader(modelMigrationClassLoader)
        Script script = slurper.classLoader.parseClass(oldParameterization).newInstance()

        try {
            ConfigObject configObject = slurper.parse(script)
            Parameterization parameterization = ParameterizationHelper.createParameterizationFromConfigObject(configObject, "MigratableCoreParams")
            parameterization.save()
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(script.class)
        }
    }

    void testOldModel() {
        Parameterization old = new Parameterization("MigratableCoreParams")
        old.modelClass = MigratableCoreModel
        ModelMigrator.doWithContextClassLoader modelMigrationClassLoader, {
            old.load()
        }

        assertEquals 6, old.parameterHolders.size() //as saved above
        assertNotNull old.parameterHolders.find { it.path == "composite:parmTimeMode" }
        ParameterObjectParameterHolder parmStrategy = old.parameterHolders.find { it.path == "composite:parmStrategy" }
        assertEquals 2, parmStrategy.classifierParameters.size()
        assertEquals "THREE_COLUMNS", parmStrategy.classifier.typeName

        MultiDimensionalParameterHolder table = parmStrategy.classifierParameters.get("table")
        assertEquals 3, table.businessObject.valueColumnCount

        ModelMigrator migrator = new ModelMigrator(MigratableCoreModel)
        migrator.migrateParameterizations()

        old.load()
        assertEquals 5, old.parameterHolders.size() //composite:parmTimeMode should have been removed
        assertNull old.parameterHolders.find { it.path == "composite:parmTimeMode" }
        parmStrategy = old.parameterHolders.find { it.path == "composite:parmStrategy" }
        assertEquals "TWO_COLUMNS", parmStrategy.classifier.typeName
        assertEquals 1, parmStrategy.classifierParameters.size()

        table = parmStrategy.classifierParameters.get("table")
        assertEquals 2, table.businessObject.valueColumnCount
    }
}
