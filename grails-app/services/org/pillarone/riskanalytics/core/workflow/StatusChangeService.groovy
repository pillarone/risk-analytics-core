package org.pillarone.riskanalytics.core.workflow

import org.pillarone.riskanalytics.core.simulation.item.Parameterization

import static org.pillarone.riskanalytics.core.workflow.Status.*
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

class StatusChangeService {

    private Map<Status, Closure> actions = [
            (NONE): { Parameterization parameterization ->
                throw new IllegalStateException("Cannot change status to ${NONE.getDisplayName()}")
            },
            (DATA_ENTRY): { Parameterization parameterization ->
                if (parameterization.status == IN_REVIEW) {
                    parameterization.status = Status.REJECTED
                    parameterization.save()
                }
                parameterization = incrementVersion(parameterization, parameterization.status == NONE)
                parameterization.status = DATA_ENTRY
                parameterization.save()
                return parameterization
            },
            (IN_REVIEW): { Parameterization parameterization ->
                parameterization.status = IN_REVIEW
                parameterization.save()
                return parameterization
            },
            (IN_PRODUCTION): { Parameterization parameterization ->
                parameterization.status = IN_PRODUCTION
                parameterization.save()
                return parameterization
            }
    ]

    Parameterization changeStatus(Parameterization parameterization, Status to) {
        return actions.get(to).call(parameterization)
    }

    //TODO: re-use MIF
    private Parameterization incrementVersion(Parameterization item, boolean newR) {
        Parameterization newItem = new Parameterization(item.name)

        List newParameters = ParameterizationHelper.copyParameters(item.parameters)
        newParameters.each {
            newItem.addParameter(it)
        }
        newItem.periodCount = item.periodCount
        newItem.periodLabels = item.periodLabels
        newItem.modelClass = item.modelClass
        newItem.versionNumber = newR ? new VersionNumber("R1") : VersionNumber.incrementVersion(item)

        def newId = newItem.save()
        newItem.load()
        return newItem
    }
}
