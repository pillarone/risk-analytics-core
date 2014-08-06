package org.pillarone.riskanalytics.core.upload

import com.google.common.base.Preconditions
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.SimulationProfile
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder

class UploadValidationService {

    int counter = 0

    List<UploadValidationError> validate(Simulation simulation, SimulationProfile simulationProfile) {
        Preconditions.checkNotNull(simulation)
        List<UploadValidationError> errors = []
        validateSimulationProfile(simulationProfile, errors)
        if (errors) {
            return errors
        }
        validateRandomSeed(simulation, simulationProfile, errors)
        validateNumberOfIterations(simulation, simulationProfile, errors)
        validateTemplate(simulation, simulationProfile, errors)
        validateRuntimeParameters(simulation, simulationProfile, errors)
        errors
    }

    private void validateRuntimeParameters(Simulation simulation, SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (!simulation.loaded) {
            simulation.load()
        }
        simulation.notDeletedParameterHolders.each { ParameterHolder holder ->
            ParameterHolder profileHolder = simulationProfile.getParameterHolder(holder.path, 0)
            if (!profileHolder) {
                errors.add(new UploadValidationError(error: 'profi'))
                return
            }
            if (holder.businessObject != profileHolder.businessObject) {
                errors.add(new UploadValidationError(path: holder.path, error: 'busi'))
            }
        }
    }

    private void validateTemplate(Simulation simulation, SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (simulation.template != simulationProfile.template) {
            errors.add(new UploadValidationError(error: 'temp'))
        }
    }

    private void validateNumberOfIterations(Simulation simulation, SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (simulation.numberOfIterations != simulationProfile.numberOfIterations) {
            errors.add(new UploadValidationError(error: 'number'))
        }
    }

    private void validateRandomSeed(Simulation simulation, SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (simulation.randomSeed != simulationProfile.randomSeed) {
            errors.add(new UploadValidationError(error: 'rand'))
        }
    }

    private void validateSimulationProfile(SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (!simulationProfile) {
            errors.add(new UploadValidationError(error: 'no simulation profile'))
        }
    }


}
