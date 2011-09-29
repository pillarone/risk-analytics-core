package org.pillarone.riskanalytics.core.dataaccess

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.output.*

class ResultAccessor {

    static Log LOG = LogFactory.getLog(ResultAccessor)

    static List getRawData(SimulationRun simulationRun) {

        return SingleValueResult.findAllBySimulationRun(simulationRun)
    }

    static String exportCsv(SimulationRun simulationRun) {
        String fileName = GroovyUtils.getExportFileName(simulationRun)
        if (new File(fileName).exists()) return fileName
        Sql sql = new Sql(simulationRun.dataSource)
        try {
            sql.execute("select concat_ws(',',cast(s.iteration as char),cast(s.period as char),mapping.path_name, cast(s.value as char))  INTO OUTFILE ? from single_value_result as s, path_mapping as mapping  where s.simulation_run_id ='" + simulationRun.id + "' and mapping.id=s.path_id", [fileName])
        } catch (Exception ex) {
            LOG.error "exception occured during export simulation as csv : $ex"
            return null
        }
        return fileName
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
        }
        else {
            def res = SingleValueResult.executeQuery("SELECT AVG(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                    " WHERE s.path.pathName = ? AND " +
                    "s.collector.collectorName = ? AND " +
                    "s.field.fieldName = ? AND " +
                    "s.period = ? AND " +
                    "s.simulationRun.id = ?", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
            return res[0]
        }
    }

    static Map<String, Double> getMeans(SimulationRun simulationRun, String collectorName) {
        if (simulationRun.iterations == 1) {
            return DeterminsiticResultAccessor.getSingleValues(simulationRun, collectorName)
        }
        return PostSimulationCalculationAccessor.getKeyFigureResults(simulationRun, collectorName, PostSimulationCalculation.MEAN)
    }

    static Double getAvgOfSingleValueResult(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        def res = SingleValueResult.executeQuery("SELECT AVG(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        return res[0]
    }


    static Double getMin(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        String query = "SELECT MIN(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?"
        def res = SingleValueResult.executeQuery(query, [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        return res[0]
    }

    static Double getMax(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        String query = "SELECT MAX(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?"
        def res = SingleValueResult.executeQuery(query, [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
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
        }
        else {
            double[] ultimates = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            if (ultimates.size() > 0) {
                return MathUtils.calculateStandardDeviation(ultimates)
            }
            else {
                return null
            }
        }
    }

    static Double getNthOrderStatistic(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName,
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


    static Double getPercentile(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName, Double severity,
                                QuantilePerspective perspective) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, perspective.getPercentileAsString(), severity)
        if (result != null) {
            return result.result
        }
        else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            return MathUtils.calculatePercentile(values, severity, perspective)
        }
    }

    static Double getPercentile(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName, Double severity) {
        return getPercentile(simulationRun, periodIndex, path, collectorName, fieldName, severity, QuantilePerspective.LOSS)
    }

    static Double getVar(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName, Double severity) {
        return getVar(simulationRun, periodIndex, path, collectorName, fieldName, severity, QuantilePerspective.LOSS)
    }

    static Double getVar(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName, Double severity,
                         QuantilePerspective perspective) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, perspective.getVarAsString(), severity)
        if (result != null) {
            return result.result
        }
        else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            return MathUtils.calculateVar(values, severity, perspective)
        }
    }

    static Double getTvar(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName, Double severity) {
        return getTvar(simulationRun, periodIndex, path, collectorName, fieldName, severity, QuantilePerspective.LOSS)
    }

    static Double getTvar(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName,
                          Double severity, QuantilePerspective perspective) {
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, path, collectorName, fieldName, perspective.getTvarAsString(), severity)
        if (result != null) {
            return result.result
        }
        else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            return MathUtils.calculateTvar(values, severity, perspective)
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
        return SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
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

    static Map<String, List> getAllValues(SimulationRun simulationRun, String collectorName) {
        Map<String, List> valuesMap = [:]
        def singleValueResults = SingleValueResult.executeQuery("SELECT s.path.pathName, s.field.fieldName, s.period,s.value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.collector.collectorName = ? and s.simulationRun.id = ? ORDER BY s.iteration ", [collectorName, simulationRun.id])
        for (def s: singleValueResults) {
            String key = s[0] + ":" + s[1] + ":" + s[2]
            List values = valuesMap[key]
            if (!values) values = []
            values << s[3]
            valuesMap[key] = values
        }
        return valuesMap
    }

    static Double getStandardDeviationValue(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName) {
        println " getStandardDeviationValue "
        double[] ultimates = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
        if (ultimates.size() > 0) {
            return MathUtils.calculateStandardDeviation(ultimates)
        }
        else {
            return null
        }
    }

//    static Map<String, Double> getKeyFigureResults(SimulationRun simulationRun, int periodIndex = 0, String path, String collectorName, String fieldName) {
    //        Map<String, List> valuesMap = [:]
    //
    //        def postSimulationCalculations = PostSimulationCalculation.executeQuery("SELECT p.path.pathName, p.field.fieldName, p.period,p.result FROM org.pillarone.riskanalytics.core.output.PostSimulationCalculation as p " +
    //                " WHERE p.collector.collectorName = ? and p.run.id = ?  and p.keyFigure = ?", [collectorName, simulationRun.id, keyFigure])
    //        for (def s: postSimulationCalculations) {
    //            String key = s[0] + ":" + s[1] + ":" + s[2]
    //            valuesMap[key] = s[3]
    //        }
    //        return valuesMap
    //    }

    public static List<Object[]> getAvgAndIsStochasticForSimulationRun(SimulationRun simulationRun, long singleCollectorId) {
        Sql sql = new Sql(simulationRun.dataSource)
        List<GroovyRowResult> rows = sql.rows("SELECT path_id, period,collector_id, field_id, AVG(value) as average, MIN(value) as minimum, MAX(value) as maximum " +
                "FROM single_value_result s " +
                " WHERE simulation_run_id = " + simulationRun.id + " AND collector_id != " + singleCollectorId +
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

    public static List getSingleValueResults(String collector, String path, String field, SimulationRun run) {
        StringBuilder sb = new StringBuilder("select  s.path.pathName, s.value, s.field.fieldName, s.iteration, s.period, s.valueIndex from ${SingleValueResult.name} as s WHERE ")
        sb.append(" s.collector.collectorName = ? AND s.field.fieldName = ? and s.path.pathName = ?   AND s.simulationRun.id = ? ")
        return SingleValueResult.executeQuery(sb.toString(), [collector, field, path, run.id])
    }

    public synchronized static List<Object[]> getResults(List<GroovyRowResult> rows, int size) {
        def result = []
        for (GroovyRowResult row in rows) {
            def array = new Object[size]
            for (int i = 0; i < size; i++) {
                array[i] = row.getAt(i)
            }
            result << array
        }
        return result
    }


    public static int getAvgAndIsStochasticForSimulationRunCount(List result) {
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