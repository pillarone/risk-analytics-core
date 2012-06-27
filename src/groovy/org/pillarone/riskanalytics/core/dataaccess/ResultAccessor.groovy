package org.pillarone.riskanalytics.core.dataaccess

import groovy.sql.Sql
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.output.*
import groovy.sql.GroovyRowResult

abstract class ResultAccessor {

    private static final Log LOG = LogFactory.getLog(ResultAccessor)

    static List<SingleValueResult> getAllResults(SimulationRun simulationRun) {
        return SingleValueResult.findAllBySimulationRun(simulationRun)
    }

    static String exportCsv(SimulationRun simulationRun) {
        String fileName = GroovyUtils.getExportFileName(simulationRun)
        File file = new File(fileName)
        if (file.exists()) {
            file.delete()
        }
        StringBuilder fileContent = new StringBuilder()
        Sql sql = new Sql(simulationRun.dataSource)
        try {
            List<GroovyRowResult> rows = sql.rows("select concat_ws(',',cast(s.iteration as char),cast(s.period as char),mapping.path_name, cast(s.value as char), cm.collector_name, from_unixtime(date / 1000))  AS data from single_value_result as s, path_mapping as mapping, collector_mapping cm  where s.simulation_run_id ='" + simulationRun.id + "' and mapping.id=s.path_id and cm.id=s.collector_id")
            for (GroovyRowResult rowResult in rows) {
                fileContent.append(rowResult["data"]).append("\n")
            }
        } catch (Exception ex) {
            LOG.error("CSV export failed : ${ex.message}", ex)
            return null
        }
        file.text = fileContent.toString()
        return fileName
    }

    static List<ResultPathDescriptor> getDistinctPaths(SimulationRun run) {
        CollectorMapping singleCollector = CollectorMapping.findByCollectorName(SingleValueCollectingModeStrategy.IDENTIFIER)
        if (singleCollector == null) {
            throw new IllegalStateException("Single collector mapping not found")
        }
        List<Object[]> queryResult = (List<Object[]>) SingleValueResult.executeQuery("SELECT DISTINCT s.path, s.field, s.collector, s.period FROM SingleValueResult s WHERE s.simulationRun = ? AND s.collector != ?", [run, singleCollector])
        return queryResult.collect { new ResultPathDescriptor(it[0], it[1], it[2], it[3]) }
    }


    static Double getMean(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        PostSimulationCalculation result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, pathName, collectorName, fieldName, PostSimulationCalculation.MEAN)
        if (result != null) {
            return result.result
        }
        else {
            List<Double> allValues = getValues(simulationRun, periodIndex, pathName, collectorName, fieldName)
            return allValues.sum() / simulationRun.iterations
        }
    }

    static Double getMin(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        String query = "SELECT MIN(value), COUNT(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?"
        def res = SingleValueResult.executeQuery(query, [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        int count = res[0][1]
        Double min = res[0][0]
        if (count < simulationRun.iterations) {
            min = Math.min(min, 0)
        }
        return min
    }

    static Double getMax(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        String query = "SELECT MAX(value), COUNT(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?"
        def res = SingleValueResult.executeQuery(query, [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        int count = res[0][1]
        Double max = res[0][0]
        if (count < simulationRun.iterations) {
            max = Math.max(max, 0)
        }
        return max
    }


    static boolean hasDifferentValues(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        if (simulationRun.iterations == 1) {
            return false
        }
        List<Double> values = getValues(simulationRun, periodIndex, pathName, collectorName, fieldName)
        if (values.size() < simulationRun.iterations) {
            values << 0d
        }
        return values.max() != values.min()

    }

    static Double getStdDev(SimulationRun simulationRun, int periodIndex, String path, String collectorName, String fieldName) {
        PostSimulationCalculation result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, PostSimulationCalculation.STDEV)
        if (result != null) {
            return result.result
        }
        else {
            double[] sortedValues = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            if (sortedValues.size() > 0) {
                return MathUtils.calculateStandardDeviation(sortedValues)
            }
            else {
                return null
            }
        }
    }

    static Double getNthOrderStatistic(SimulationRun simulationRun, int periodIndex, String path, String collectorName,
                                       String fieldName, double percentage, CompareOperator compareOperator) {
        double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
        double lowestPercentage = 100d / values.size()
        if ((compareOperator.equals(CompareOperator.LESS_THAN) && percentage <= lowestPercentage)
                || compareOperator.equals(CompareOperator.GREATER_THAN) && percentage == 100) {
            return null
        }
        Double rank = simulationRun.getIterations() * percentage * 0.01

        Integer index = rank.toInteger()
        if (rank - index > 0) {
            switch (compareOperator) {
                case CompareOperator.GREATER_THAN:
                case CompareOperator.GREATER_EQUALS:
                    index++
                    break
            }
        }
        else if (rank - index == 0) {
            switch (compareOperator) {
                case CompareOperator.GREATER_THAN:
                    index++
                    break
                case CompareOperator.LESS_THAN:
                    index--
                    break
            }
        }
        else if (rank - index < 0) {
            switch (compareOperator) {
                case CompareOperator.LESS_THAN:
                case CompareOperator.LESS_EQUALS:
                    index--
                    break
            }
        }
        return rank == 0 ? values[0] : values[--index]       // -1 as array index starts with 0
    }


    static Double getPercentile(SimulationRun simulationRun, int periodIndex, String path, String collectorName, String fieldName, Double severity,
                                QuantilePerspective perspective) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, perspective.getPercentileAsString(), severity)
        if (result != null) {
            return result.result
        }
        else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            if (values.length == 0) {
                return null
            }
            return MathUtils.calculatePercentile(values, severity, perspective)
        }
    }

    static Double getVar(SimulationRun simulationRun, int periodIndex, String path, String collectorName, String fieldName, Double severity,
                         QuantilePerspective perspective) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, perspective.getVarAsString(), severity)
        if (result != null) {
            return result.result
        }
        else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            if (values.length == 0) {
                return null
            }
            return MathUtils.calculateVar(values, severity, perspective)
        }
    }

    static Double getTvar(SimulationRun simulationRun, int periodIndex, String path, String collectorName, String fieldName,
                          Double severity, QuantilePerspective perspective) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, perspective.getTvarAsString(), severity)
        if (result != null) {
            return result.result
        }
        else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            if (values.length == 0) {
                return null
            }
            return MathUtils.calculateTvar(values, severity, perspective)
        }
    }

    static double[] getValuesSorted(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        return fillWithZeroes(simulationRun, (double[]) SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.period = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.simulationRun.id = ? ORDER BY value", [pathName, periodIndex, collectorName, fieldName, simulationRun.id]))
    }

    static double[] getValues(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        return fillWithZeroes(simulationRun, (double[]) SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.period = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.simulationRun.id = ? ORDER BY s.iteration", [pathName, periodIndex, collectorName, fieldName, simulationRun.id]))
    }

    public static List getSingleValueResults(String collector, String path, String field, SimulationRun run) {
        StringBuilder sb = new StringBuilder("select  s.path.pathName, s.value, s.field.fieldName, s.iteration, s.period, s.valueIndex from ${SingleValueResult.name} as s WHERE ")
        sb.append(" s.collector.collectorName = ? AND s.field.fieldName = ? and s.path.pathName = ?   AND s.simulationRun.id = ? ")
        return SingleValueResult.executeQuery(sb.toString(), [collector, field, path, run.id])
    }

    public static List<SingleValueResult> getSingleValueResultsWithDateSkipZeroes(SimulationRun run, int periodIndex, String pathName, String collectorName, String fieldName) {
        // todo: would have preferred to use SymbolicValueResult.findAll() here, but that didn't work since there
        // was some kind of 'version' field that all of a sudden showed up in the resulting SQL... ? I am at loss.
        return SingleValueResult.createCriteria().list {
            eq("simulationRun", run)
            eq("period", periodIndex)
            eq("path", org.pillarone.riskanalytics.core.output.PathMapping.findByPathName(pathName))
            eq("field", org.pillarone.riskanalytics.core.output.FieldMapping.findByFieldName(fieldName))
            eq("collector", org.pillarone.riskanalytics.core.output.CollectorMapping.findByCollectorName(collectorName))
            not { eq("value", 0.0d) }
        }
    }

    public static Double getUltimatesForOneIteration(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName, int iteration) {
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

    private static double[] fillWithZeroes(SimulationRun run, double[] results) {
        // number of iterations may be smaller as results length if a single collector is used
        if (run.iterations <= results.length || results.length == 0) return results

        double[] result = new double[run.iterations]
        System.arraycopy(results, 0, result, 0, results.length)
        for (int i = results.length; i < result.length; i++) {
            result[i] = 0d
        }
        Arrays.sort(result)
        return result
    }

}