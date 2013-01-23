package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.parameterization.ApplicableParameter
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class ModelMigrationApplicableParameter extends ApplicableParameter {

    private static Log LOG = LogFactory.getLog(ModelMigrationApplicableParameter)

    ModelMigrationApplicableParameter(ApplicableParameter parameter) {
        component = parameter.component
        parameterPropertyName = parameter.parameterPropertyName
        parameterValue = parameter.parameterValue
    }

    @Override
    void apply() {
        try {
            super.apply()
        } catch (MissingPropertyException e) {
            LOG.info "Property $parameterPropertyName has been removed from ${component.class.name} - ignoring"
        }

    }


}
