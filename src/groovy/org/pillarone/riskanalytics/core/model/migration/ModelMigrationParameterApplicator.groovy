package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.parameterization.ApplicableParameter
import org.pillarone.riskanalytics.core.model.Model


class ModelMigrationParameterApplicator extends ParameterApplicator {

    @Override
    protected ApplicableParameter createApplicableParameter(Model model, String path, Object parameterValue) {
        ApplicableParameter parameter = super.createApplicableParameter(model, path, parameterValue)
        return new ModelMigrationApplicableParameter(parameter)
    }


}
