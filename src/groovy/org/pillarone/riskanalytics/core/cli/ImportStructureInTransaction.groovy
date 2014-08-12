package org.pillarone.riskanalytics.core.cli

import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration

class ImportStructureInTransaction {

    static void importStructure(SimulationConfiguration configuration) {
        ModelStructureDAO.withTransaction { status ->
            List<String> modelFilter = new ArrayList<String>(1)
            String modelName = configuration.simulation.modelClass.simpleName
            int indexOfModel = modelName.lastIndexOf("Model")
            String withoutModel = indexOfModel == -1 ? modelName : modelName.substring(0, indexOfModel)
            modelFilter.add(withoutModel)
            new ModelStructureImportService().compareFilesAndWriteToDB(modelFilter)
        }
    }
}
