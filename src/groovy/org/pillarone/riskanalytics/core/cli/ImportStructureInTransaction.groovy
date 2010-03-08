package org.pillarone.riskanalytics.core.cli

import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService
import org.pillarone.riskanalytics.core.ModelStructureDAO


class ImportStructureInTransaction {


    public static void importStructure(SimulationConfiguration configuration) {
        ModelStructureDAO.withTransaction {status ->
            List<String> modelFilter = new ArrayList<String>(1);
            String modelName = configuration.getSimulation().getModelClass().getSimpleName();
            modelFilter.add(modelName.substring(0, modelName.lastIndexOf("Model")));
            new ModelStructureImportService().compareFilesAndWriteToDB(modelFilter);
        }
    }
}
