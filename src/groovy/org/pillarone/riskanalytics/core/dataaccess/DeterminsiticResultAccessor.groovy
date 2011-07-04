package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.SymbolicValueResult

public class DeterminsiticResultAccessor {


    static Double getSingleValueFromView(SimulationRun simulationRun, String fieldName, String collectorName, String pathName, int periodIndex, int iteration = 1) {
        def value = SymbolicValueResult.executeQuery("SELECT value FROM ${SymbolicValueResult.name} " +
                "WHERE iteration = ? AND path = ? AND " +
                "collector = ? AND " +
                "field = ? AND " +
                "period = ? AND " +
                "simulation_run_id = ?", [iteration, pathName, collectorName, fieldName, periodIndex, simulationRun.id])

        return value[0]
    }


    static Map<String, Double> getSingleValues(SimulationRun simulationRun, String collectorName, int iteration = 1) {
        Map<String, List> valuesMap = [:]

        def symbolicValueResults = SymbolicValueResult.executeQuery("SELECT path, field, period, value FROM ${SymbolicValueResult.name} " +
                "WHERE iteration = ?  AND " +
                "collector = ? AND " +
                "simulation_run_id = ?", [iteration, collectorName, simulationRun.id])
        for (def s: symbolicValueResults) {
            String key = s[0] + ":" + s[1] + ":" + s[2]
            valuesMap[key] = s[3]
        }
        return valuesMap
    }

    static List getAllPeriodValuesFromView(SimulationRun simulationRun, String fieldName, String collectorName, String pathName, int iteration = 1) {
        def value = SymbolicValueResult.executeQuery("SELECT value FROM ${SymbolicValueResult.name} " +
                "WHERE iteration = ? AND path = ? AND " +
                "collector = ? AND " +
                "field = ? AND " +
                "simulation_run_id = ? order by period", [iteration, pathName, collectorName, fieldName, simulationRun.id])

        return value
    }

}