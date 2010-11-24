package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.output.*
import groovy.sql.Sql
import groovy.sql.GroovyRowResult

class ResultAccessor {

    static List getRawData(SimulationRun simulationRun) {

        return SingleValueResult.findAllBySimulationRun(simulationRun)
    }

    static List getPaths(SimulationRun simulationRun) {
        return SingleValueResult.executeQuery("SELECT DISTINCT p.pathName " +
                "FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s, org.pillarone.riskanalytics.core.output.PathMapping as p " +
                "WHERE s.path.id = p.id AND s.simulationRun.id = " + simulationRun.id)
    }


    static Double getMean(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        if (simulationRun.iterations == 1) {
            return DeterminsiticResultAccessor.getSingleValueFromView(simulationRun, fieldName, collectorName, pathName, periodIndex)
        }


        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, pathName, collectorName, fieldName, PostSimulationCalculation.MEAN)
        if (result != null) {
            return result.result
        } else {
            def res = SingleValueResult.executeQuery("SELECT AVG(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                    " WHERE s.path.pathName = ? AND " +
                    "s.collector.collectorName = ? AND " +
                    "s.field.fieldName = ? AND " +
                    "s.period = ? AND " +
                    "s.simulationRun.id = ?", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
            return res[0]
        }
    }


    static Double getMin(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        def res = SingleValueResult.executeQuery("SELECT MIN(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        return res[0]
    }

    static Double getMax(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        def res = SingleValueResult.executeQuery("SELECT MAX(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        return res[0]
    }


    public static boolean hasDifferentValues(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        if (simulationRun.iterations == 1) {
            return false
        }
        List values = getValues(simulationRun, periodIndex, pathName, collectorName, fieldName)
        return values.max() != values.min()
        //todo: fix query! does not work properly when called from RTTM
        /*Statement stmt = simulationRun.dataSource.connection.createStatement()
       ResultSet res = stmt.executeQuery("SELECT min(value) = max(value) as isStochastic FROM single_value_result s, path_mapping p, field_mapping f, collector_mapping c WHERE " +
               "s.path_id = p.id " +
               "AND s.field_id = f.id " +
               "AND s.collector_id = c.id " +
               "AND p.path_name = '" + pathName + "'" +
               "AND f.field_name = '" + fieldName + "'" +
               "AND c.collector_name = '" + collectorName + "'" +
               "AND s.id = '"+ simulationRun.id + "'" +
               "AND s.period = '"+ periodIndex + "'"
       )
       res.next()
       return !res.getBoolean("isStochastic")*/
    }

    static Double getStdDev(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, PostSimulationCalculation.STDEV)
        if (result != null) {
            return result.result
        } else {
            double[] ultimates = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            if (ultimates.size() > 0) {
                return MathUtils.calculateStandardDeviation(ultimates)
            } else {
                return null
            }
        }
    }

    static Double getNthOrderStatistic(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName,
                                       String fieldName, double percentage, boolean countingFromLowerEnd) {
        double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
        if (countingFromLowerEnd) {
            int rank = (int) (simulationRun.getIterations() * percentage * 0.01)
            return rank == 0 ?  null : values[rank - 1]
        }
        else {
            int rank = (int) (simulationRun.getIterations() * (1 - percentage) * 0.01)
            return values[-rank]
        }
    }

    static Double getPercentile(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName, Double percentile) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, PostSimulationCalculation.PERCENTILE, percentile)
        if (result != null) {
            return result.result
        } else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            return MathUtils.calculatePercentile(values, percentile)
        }
    }

    static Double getVar(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName, Double percentile) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, PostSimulationCalculation.VAR, percentile)
        if (result != null) {
            return result.result
        } else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            return MathUtils.calculateVar(values, percentile)
        }
    }


    static Double getTvar(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName, Double percentile) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, PostSimulationCalculation.TVAR, percentile)
        if (result != null) {
            return result.result
        } else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            return MathUtils.calculateTvar(values, percentile)
        }
    }

    static List getValuesSorted(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.period = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.simulationRun.id = ? ORDER BY value", [pathName, periodIndex, collectorName, fieldName, simulationRun.id])
    }

    static List getValuesSorted(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.id = ? AND " +
                "s.period = ? AND " +
                "s.collector.id = ? AND " +
                "s.field.id = ? AND " +
                "s.simulationRun.id = ? ORDER BY value", [pathId, period, collectorId, fieldId, simulationRun.id])
    }

    static List getValues(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.period = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.simulationRun.id = ? ORDER BY s.iteration", [pathName, periodIndex, collectorName, fieldName, simulationRun.id])
    }

    static List getValues(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.id = ? AND " +
                "s.period = ? AND " +
                "s.collector.id = ? AND " +
                "s.field.id = ? AND " +
                "s.simulationRun.id = ? ORDER BY s.iteration", [pathId, period, collectorId, fieldId, simulationRun.id])
    }



    public static List<Object[]> getAvgAndIsStochasticForSimulationRun(SimulationRun simulationRun) {
        Sql sql = new Sql(simulationRun.dataSource)
        List<GroovyRowResult> rows = sql.rows("SELECT path_id, period,collector_id, field_id, AVG(value) as average, MIN(value) as minimum, MAX(value) as maximum " +
                "FROM single_value_result s " +
                " WHERE simulation_run_id = " + simulationRun.id +
                " GROUP BY period, path_id, collector_id, field_id")
        def result = []
        for (GroovyRowResult row in rows) {
            def array = new Object[7]
            for (int i = 0; i < 7; i++) {
                array[i] = row.getAt(i)
            }
            result << array
        }
        sql.close()
        return result
    }



    public static int getAvgAndIsStochasticForSimulationRunCount(SimulationRun simulationRun) {
        List result = getAvgAndIsStochasticForSimulationRun(simulationRun)
        int count = 0
        for (array in result) {
            int isStochastic = array[5] == array[6] ? 1 : 0
            if (isStochastic == 0)
                count++
        }
        return count
    }


    public static Double getUltimatesForOneIteration(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName, int iteration) {
        // TODO (Jul 13, 2009, msh): Why do we use a Criteria here ? See getSingleValueFromView(...)
        PathMapping path = PathMapping.findByPathName(pathName)
        FieldMapping field = FieldMapping.findByFieldName(fieldName)
        CollectorMapping collector = CollectorMapping.findByCollectorName(collectorName)
        def c = SingleValueResult.createCriteria()
        List res = c.list {
            eq("simulationRun", simulationRun)
            eq("period", periodIndex)
            eq("path", path)
            eq("field", field)
            eq("collector", collector)
            eq("iteration", iteration)
            projections {
                groupProperty("iteration")
                sum("value")
            }
        }
        if (res.size() == 0) {
            return null
        }
        return res[0][1]
    }
}