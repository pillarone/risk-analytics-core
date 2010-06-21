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
            Sql sql = new Sql(simulationRun.dataSource)
            def res = sql.firstRow(
                    "SELECT AVG(iteration.total) AS average FROM ( " +
                            "SELECT SUM(svr.value) AS total " +
                            "FROM single_value_result svr, path_mapping pm, field_mapping fm " +
                            "WHERE svr.simulation_run_id = ? AND svr.path_id = pm.id AND pm.path_name = ? AND svr.field_id = fm.id AND fm.field_name = ? AND period = ? " +
                            "GROUP BY iteration) AS iteration", [simulationRun.id, pathName, fieldName, periodIndex])

            def average = res.average
            sql.close()
            return average
        }
    }


    static Double getMin(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        Sql sql = new Sql(simulationRun.dataSource)
        def res = sql.firstRow(
                "SELECT MIN(iteration.total) AS minimum FROM ( " +
                        "SELECT SUM(svr.value) AS total " +
                        "FROM single_value_result svr, path_mapping pm, field_mapping fm " +
                        "WHERE svr.simulation_run_id = ? AND svr.path_id = pm.id AND pm.path_name = ? AND svr.field_id = fm.id AND fm.field_name = ? AND period = ? " +
                        "GROUP BY iteration) AS iteration", [simulationRun.id, pathName, fieldName, periodIndex])
        def minimum = res.minimum
        sql.close()
        return minimum
    }

    static Double getMax(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        Sql sql = new Sql(simulationRun.dataSource)
        def res = sql.firstRow(
                "SELECT MAX(iteration.total) AS maximum FROM ( " +
                        "SELECT SUM(svr.value) AS total " +
                        "FROM single_value_result svr, path_mapping pm, field_mapping fm " +
                        "WHERE svr.simulation_run_id = ? AND svr.path_id = pm.id AND pm.path_name = ? AND svr.field_id = fm.id AND fm.field_name = ? AND period = ? " +
                        "GROUP BY iteration) AS iteration", [simulationRun.id, pathName, fieldName, periodIndex])

        def maximum = res.maximum
        sql.close()
        return maximum
    }


    public static boolean hasDifferentValues(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        if (simulationRun.iterations == 1) {
            return false
        }
        return getMax(simulationRun, periodIndex, pathName, collectorName, fieldName) != getMin(simulationRun, periodIndex, pathName, collectorName, fieldName)
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
        Sql sql = new Sql(simulationRun.dataSource)
        List<GroovyRowResult> res = sql.rows(
                "SELECT SUM(svr.value) AS total " +
                        "FROM single_value_result svr, path_mapping pm, field_mapping fm " +
                        "WHERE svr.simulation_run_id = ? AND svr.path_id = pm.id AND pm.path_name = ? AND svr.field_id = fm.id AND fm.field_name = ? AND period = ? " +
                        "GROUP BY iteration ORDER BY total", [simulationRun.id, pathName, fieldName, periodIndex])

        List values = res.collect { it.getAt("total") }
        sql.close()
        return values
    }

    static List getValuesSorted(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        Sql sql = new Sql(simulationRun.dataSource)
        List<GroovyRowResult> res = sql.rows(
                "SELECT SUM(value) AS total " +
                        "FROM single_value_result " +
                        "WHERE simulation_run_id = ? AND path_id = ? AND field_id = ? AND period = ? " +
                        "GROUP BY iteration ORDER BY total", [simulationRun.id, pathId, fieldId, period])

        List values = res.collect { it.getAt("total") }
        sql.close()
        return values
    }

    static List getValues(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        Sql sql = new Sql(simulationRun.dataSource)
        List<GroovyRowResult> res = sql.rows(
                "SELECT SUM(svr.value) AS total " +
                        "FROM single_value_result svr, path_mapping pm, field_mapping fm " +
                        "WHERE svr.simulation_run_id = ? AND svr.path_id = pm.id AND pm.path_name = ? AND svr.field_id = fm.id AND fm.field_name = ? AND period = ? " +
                        "GROUP BY iteration", [simulationRun.id, pathName, fieldName, periodIndex])

        List values = res.collect { it.getAt("total") }
        sql.close()
        return values
    }

    static List getValues(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        Sql sql = new Sql(simulationRun.dataSource)
        List<GroovyRowResult> res = sql.rows(
                "SELECT SUM(value) AS total " +
                        "FROM single_value_result " +
                        "WHERE simulation_run_id = ? AND path_id = ? AND field_id = ? AND period = ? " +
                        "GROUP BY iteration", [simulationRun.id, pathId, fieldId, period])

        List values = res.collect { it.getAt("total") }
        sql.close()
        return values
    }



    public static List<Object[]> getAvgAndIsStochasticForSimulationRun(SimulationRun simulationRun) {
        Sql sql = new Sql(simulationRun.dataSource)
        List<GroovyRowResult> rows = sql.rows("SELECT iteration.path_id, iteration.period, iteration.collector_id, iteration.field_id, AVG(iteration.total) AS average, MIN(iteration.total) AS minimum, MAX(iteration.total) AS maximum FROM ( " +
                "SELECT path_id, period,collector_id, field_id, SUM(value) as total " +
                "FROM single_value_result s " +
                "WHERE simulation_run_id = ? " +
                "GROUP BY path_id, period, collector_id, field_id, iteration) AS iteration " +
                "GROUP BY period, path_id, collector_id, field_id", [simulationRun.id])
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