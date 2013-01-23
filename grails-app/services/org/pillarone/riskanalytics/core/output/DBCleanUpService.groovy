package org.pillarone.riskanalytics.core.output

class DBCleanUpService {

    boolean transactional = true

    def cleanUp() {
        println "starting db cleanup service."
        println "# simulations before cleanup: ${SimulationRun.count()}"
        SingleValueResult.executeUpdate("delete ${SingleValueResult.name}".toString())
        PostSimulationCalculation.executeUpdate("delete ${PostSimulationCalculation.name}".toString())
        SimulationRun.executeUpdate("delete ${SimulationRun.name}".toString())
        println "# simulations after cleanup: ${SimulationRun.count()}"
        println "db cleanup service finished."
    }
}
