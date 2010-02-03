package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.output.*

class PostSimulationCalculationAccessor {

    static def getResult(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName, String keyFigure) {
        PathMapping path = PathMapping.findByPathName(pathName)
        FieldMapping field = FieldMapping.findByFieldName(fieldName)
        CollectorMapping collector = CollectorMapping.findByCollectorName(collectorName)

        def c = PostSimulationCalculation.createCriteria()
        def res = c.get {
            eq('run', simulationRun)
            eq('period', periodIndex)
            eq('path', path)
            eq('field', field)
            eq('collector', collector)
            eq('keyFigure', keyFigure)
        }
        return res
    }

    static def getResult(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName, String keyFigure, def keyFigureParameter) {
        if (keyFigureParameter == null) {
            return getResult(simulationRun, periodIndex, pathName, collectorName, fieldName, keyFigure)
        } else {
            PathMapping path = PathMapping.findByPathName(pathName)
            FieldMapping field = FieldMapping.findByFieldName(fieldName)
            CollectorMapping collector = CollectorMapping.findByCollectorName(collectorName)
            def c = PostSimulationCalculation.createCriteria()
            PostSimulationCalculation res = c.get {
                eq('run', simulationRun)
                eq('period', periodIndex)
                eq('path', path)
                eq('field', field)
                eq('collector', collector)
                eq('keyFigure', keyFigure)
                eq('keyFigureParameter', keyFigureParameter as BigDecimal)
            }
            return res
        }
    }

    static List getPercentiles(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        List res = PostSimulationCalculation.executeQuery("SELECT result FROM org.pillarone.riskanalytics.core.output.PostSimulationCalculation as p " +
                " WHERE p.path.pathName = ? AND " +
                "p.collector.collectorName = ? AND " +
                "p.field.fieldName = ? AND " +
                "p.period = ? AND " +
                "p.keyFigure ='percentile' AND " +
                "p.run.id = ? order by p.keyFigureParameter asc", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        return res
    }

    static List getPDFValues(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        List res = PostSimulationCalculation.executeQuery("SELECT new Map (p.keyFigureParameter as keyFigureParameter , p.result as result) FROM org.pillarone.riskanalytics.core.output.PostSimulationCalculation as p " +
                " WHERE p.path.pathName = ? AND " +
                "p.collector.collectorName = ? AND " +
                "p.field.fieldName = ? AND " +
                "p.period = ? AND " +
                "p.keyFigure ='pdf' AND " +
                "p.run.id = ? order by p.keyFigureParameter asc", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        return res
    }

}