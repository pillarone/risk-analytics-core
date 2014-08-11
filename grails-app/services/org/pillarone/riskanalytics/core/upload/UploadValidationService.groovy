package org.pillarone.riskanalytics.core.upload

import com.google.common.base.Preconditions
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.SimulationProfile
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder

import static org.pillarone.riskanalytics.core.upload.UploadValidationError.REASON.*

class UploadValidationService {

    // Note: Sim should never be null, but profile might
    // Eg user forgot to set up profile in one model class.
    //
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
        int simulationParameterCount = simulation.notDeletedParameterHolders.size()
        int profileParameterCount = simulationProfile.notDeletedParameterHolders.size()
        if (simulationParameterCount != profileParameterCount) {
            errors.add(new UploadValidationError(
                    reason: WRONG_PARAMETER_COUNT,
                    simulationParameterValue: simulationParameterCount,
                    profileParameterValue: profileParameterCount))
            return
        }
        simulation.notDeletedParameterHolders.each { ParameterHolder holder ->
            ParameterHolder profileHolder = simulationProfile.getParameterHolder(holder.path, 0)
            def simulationParameterValue = holder.businessObject
            def simulationProfileParameterValue = profileHolder?.businessObject
            if (simulationParameterValue != simulationProfileParameterValue) {
                errors.add(new UploadValidationError(
                        path: holder.path,
                        reason: WRONG_PARAMETER,
                        simulationParameterValue: simulationParameterValue,
                        profileParameterValue: simulationProfileParameterValue))
            }
        }
    }

    private void validateTemplate(Simulation simulation, SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (simulation.template != simulationProfile.template) {
            errors.add(new UploadValidationError(reason: WRONG_TEMPLATE))
        }
    }

    private void validateNumberOfIterations(Simulation simulation, SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (simulation.numberOfIterations != simulationProfile.numberOfIterations) {
            errors.add(new UploadValidationError(
                    reason: WRONG_NUMBER_OF_ITERATION,
                    simulationParameterValue: simulation.numberOfIterations,
                    profileParameterValue: simulationProfile.numberOfIterations))
        }
    }

    private void validateRandomSeed(Simulation simulation, SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (simulation.randomSeed != simulationProfile.randomSeed) {
            errors.add(new UploadValidationError(
                    reason: WRONG_RANDOM_SEED,
                    simulationParameterValue: simulation.randomSeed,
                    profileParameterValue: simulationProfile.randomSeed))
        }
    }

    private void validateSimulationProfile(SimulationProfile simulationProfile, ArrayList<UploadValidationError> errors) {
        if (!simulationProfile) {
            errors.add(new UploadValidationError(reason: NO_PROFILE))
        }
    }
}
