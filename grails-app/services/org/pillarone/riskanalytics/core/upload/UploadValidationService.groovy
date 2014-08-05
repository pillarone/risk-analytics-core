package org.pillarone.riskanalytics.core.upload

import models.core.CoreModel
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.SimulationProfile

class UploadValidationService {

    int counter = 0

    List<UploadValidationError> validate(Simulation simulation, SimulationProfile simulationProfile) {
        if (simulation.modelClass == CoreModel) {
            return [new UploadValidationError(error: 'only application model ist allowed')]
        }
        []
    }
}
