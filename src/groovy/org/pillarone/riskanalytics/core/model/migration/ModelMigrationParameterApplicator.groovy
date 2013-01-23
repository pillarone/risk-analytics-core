package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.parameterization.ApplicableParameter
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


class ModelMigrationParameterApplicator extends ParameterApplicator {

    private static Log LOG = LogFactory.getLog(ModelMigrationParameterApplicator)

    @Override
    protected ApplicableParameter createApplicableParameter(Model model, ParameterHolder parameterHolder) {
        try {
            ApplicableParameter parameter = super.createApplicableParameter(model, parameterHolder)
            return new ModelMigrationApplicableParameter(parameter)
        } catch (Exception e) {
            LOG.info "Error creating paramater for $parameterHolder.path - will be set to null and must be set by migration: $e.message"
            return new NullApplicableParameter()
        }

    }

    protected void prepareParameter(Model model, NullApplicableParameter parameter) {
        //do nothing
    }



    @Override
    protected void prepareParameter(Model model, AbstractMultiDimensionalParameter parameterValue) {

    }


}
