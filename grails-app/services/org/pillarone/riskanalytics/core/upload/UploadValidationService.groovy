package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.SimulationProfile

class UploadValidationService {

    List<UploadValidationError> validate(Simulation simulation, SimulationProfile simulationProfile) {
        //TODO: compare runtime parameters of sim and profile
        return []
    }
}
