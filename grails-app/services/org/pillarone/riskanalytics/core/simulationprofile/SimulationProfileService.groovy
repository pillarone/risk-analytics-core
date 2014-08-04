package org.pillarone.riskanalytics.core.simulationprofile

import grails.transaction.Transactional
import org.pillarone.riskanalytics.core.SimulationProfileDAO

@Transactional
class SimulationProfileService {

    String getActiveProfileName() {
        //TODO discuss how to determine. For now take the first one
        SimulationProfileDAO.list([max: 1])?.first()?.name
    }


}
