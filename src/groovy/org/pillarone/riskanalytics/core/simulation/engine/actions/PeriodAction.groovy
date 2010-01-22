package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.DeterministicModel
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope

/**
 * The PeriodAction is responsible for executing a "simulation step" by triggering the startComponents of the model.
 * For each period its perform method is called.
 */

public class PeriodAction implements Action {

    private static Log LOG = LogFactory.getLog(PeriodAction)

    Model model
    PeriodScope periodScope
    boolean parameterValidationNeeded = true    // true for all periods of the first iteration

    /**
     * Performing the periodAction means:
     * - prepare periodScope for next period
     * - apply current parameter to model
     * - validate parameter (legacy code for initialising some components properties)
     * - trigger the models startComponents
     */
    public void perform() {
        if (model) {
            if (LOG.isDebugEnabled()) LOG.debug("performing period action")
            int periodIndex = model instanceof DeterministicModel ? 0 : periodScope.currentPeriod
            if (periodIndex != periodScope.parameterApplicator.lastInjectedPeriod) {
                periodScope.parameterApplicator.applyParameterForPeriod(periodIndex)

                if (parameterValidationNeeded) {
                    for (Component component in model.allComponents) {
                        component.validateParameterization()
                    }
                }
            }

            for (Component starter in model.startComponents) {
                starter.start()
            }
        } else {
            if (LOG.isWarnEnabled()) LOG.warn "No model instance available."
        }
        //ContinuousPeriodCounter & Scope start at 0, so the period has to be increased after its execution
        periodScope.prepareNextPeriod()
    }
}
