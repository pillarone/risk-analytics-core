package org.pillarone.riskanalytics.core.simulationprofile

import grails.transaction.Transactional
import org.pillarone.riskanalytics.core.SimulationProfileDAO
import org.pillarone.riskanalytics.core.simulation.item.SimulationProfile

@Transactional
class SimulationProfileService {

    String getActiveProfileName() {
        //TODO discuss how to determine. For now take the first one
        List<String> names = SimulationProfileDAO.createCriteria().list {
            eq('forPublic', true)
            order('id', 'desc')
            maxResults(1)
            projections {
                property('name')
            }
        }
        names ? names.first() : null
    }

    Map<Class, SimulationProfile> getSimulationProfilesGroupedByModelClass(String simulationProfileName) {
        Map<Class, SimulationProfile> result = [:]
        SimulationProfileDAO.findAllByName(simulationProfileName).collect {
            SimulationProfile simulationProfile = new SimulationProfile(it.name, getClass().classLoader.loadClass(it.modelClassName))
            simulationProfile.load()
            simulationProfile
        }.each {
            result[it.modelClass] = it
        }
        result
    }

    List<String> getSimulationProfileNames() {
        SimulationProfileDAO.createCriteria().list {
            projections {
                property('name')
            }
            order('name')
        }.unique()
    }

}
