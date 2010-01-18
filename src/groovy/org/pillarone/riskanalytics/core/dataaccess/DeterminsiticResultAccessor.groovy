package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.SymbolicValueResult

public class DeterminsiticResultAccessor {


    static Double getSingleValueFromView(SimulationRun simulationRun, String fieldName, String collectorName, String pathName, int periodIndex, int iteration = 0) {
        def value = SymbolicValueResult.executeQuery("SELECT value FROM ${SymbolicValueResult.name} " +
            "WHERE iteration = ? AND path = ? AND " +
            "collector = ? AND " +
            "field = ? AND " +
            "period = ? AND " +
            "simulation_run_id = ?", [iteration, pathName, collectorName, fieldName, periodIndex, simulationRun.id])

        return value[0]
    }

    static List getAllPeriodValuesFromView(SimulationRun simulationRun, String fieldName, String collectorName, String pathName, int iteration = 0) {
        def value = SymbolicValueResult.executeQuery("SELECT value FROM ${SymbolicValueResult.name} " +
            "WHERE iteration = ? AND path = ? AND " +
            "collector = ? AND " +
            "field = ? AND " +
            "simulation_run_id = ? order by period", [iteration, pathName, collectorName, fieldName, simulationRun.id])

        return value
    }

}