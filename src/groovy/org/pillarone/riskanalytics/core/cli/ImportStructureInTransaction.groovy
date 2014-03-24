package org.pillarone.riskanalytics.core.cli

import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration

class ImportStructureInTransaction {

    public static void importStructure(SimulationConfiguration configuration) {
        ModelStructureDAO.withTransaction { status ->
            List<String> modelFilter = new ArrayList<String>(1)
            String modelName = configuration.simulation.modelClass.simpleName
            modelFilter.add(modelName.substring(0, modelName.lastIndexOf("Model")))
            new ModelStructureImportService().compareFilesAndWriteToDB(modelFilter)
        }
    }
}
