package org.pillarone.riskanalytics.core.model.migration

import models.migratableCore.MigratableCoreModel
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.fileimport.ModelFileImportService
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder
import org.pillarone.riskanalytics.core.example.migration.ResultViewMode
import org.pillarone.riskanalytics.core.simulation.item.parameter.MultiDimensionalParameterHolder

class ModelMigratorTests extends GroovyTestCase {

    void setUp() {
        new ModelFileImportService().compareFilesAndWriteToDB(["MigratableCore"])
        new ModelStructureImportService().compareFilesAndWriteToDB(["MigratableCore"])
        new ResultConfigurationImportService().compareFilesAndWriteToDB(["MigratableCore"])

        //fake an old parameterization
        Parameterization old = new Parameterization("MigratableCoreParams")
        old.periodCount = 1
        old.modelClass = MigratableCoreModel
        ParameterObjectParameterHolder holder = ParameterHolderFactory.getHolder("composite:parmStrategy", 0, TestConstraintsTableType.getStrategy(org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.THREE_COLUMNS, ["table": new org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([["S1", "S2"], ["Motor", "Engine"], [100.0, 80.0]]), ["id", "type", "value"], org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('constrained table')), "mode": org.pillarone.riskanalytics.core.example.migration.ResultViewMode.INCREMENTAL,]))
        holder.classifierParameters.put("mode", ParameterHolderFactory.getHolder("${holder.path}:mode", 0, ResultViewMode.INCREMENTAL))
        old.addParameter(holder)
        old.addParameter(ParameterHolderFactory.getHolder("composite:parmTimeMode", 0, org.pillarone.riskanalytics.core.example.migration.TimeMode.PERIOD))
        old.addParameter(ParameterHolderFactory.getHolder("dynamic:subOpt1:parmStrategy", 0, TestConstraintsTableType.getStrategy(org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.THREE_COLUMNS, ["table": new org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([["S1", "S2"], ["Motor", "Engine"], [100.0, 80.0]]), ["id", "type", "value"], org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('constrained table')), "mode": org.pillarone.riskanalytics.core.example.migration.ResultViewMode.INCREMENTAL,])))
        old.addParameter(ParameterHolderFactory.getHolder("dynamic:subOpt1:parmTimeMode", 0, org.pillarone.riskanalytics.core.example.migration.TimeMode.PERIOD))
        old.addParameter(ParameterHolderFactory.getHolder("exampleInputOutputComponent:parmParameterObject", 0, ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])))
        old.addParameter(ParameterHolderFactory.getHolder("exampleInputOutputComponent:parmNewParameterObject", 0, ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])))

        old.save()
    }

    void testOldModel() {
        Parameterization old = new Parameterization("MigratableCoreParams")
        old.modelClass = MigratableCoreModel
        old.load()

        assertEquals 6, old.parameterHolders.size() //as saved above
        assertNotNull old.parameterHolders.find { it.path == "composite:parmTimeMode" }
        ParameterObjectParameterHolder parmStrategy = old.parameterHolders.find { it.path == "composite:parmStrategy" }
        assertEquals 2, parmStrategy.classifierParameters.size()

        MultiDimensionalParameterHolder table = parmStrategy.classifierParameters.get("table")
        assertEquals 3, table.businessObject.valueColumnCount

        ModelMigrator migrator = new ModelMigrator(MigratableCoreModel)
        migrator.migrateParameterizations()

        old.load()
        assertEquals 5, old.parameterHolders.size() //composite:parmTimeMode should have been removed
        assertNull old.parameterHolders.find { it.path == "composite:parmTimeMode" }
        parmStrategy = old.parameterHolders.find { it.path == "composite:parmStrategy" }
        assertEquals 1, parmStrategy.classifierParameters.size()

        table = parmStrategy.classifierParameters.get("table")
        assertEquals 2, table.businessObject.valueColumnCount
    }
}
