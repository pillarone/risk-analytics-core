package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.output.*

import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper

abstract class ResultAccessor {

    private static HashMap<String, Integer> pathCache = new HashMap<String, Integer>();
    private static HashMap<String, Integer> fieldCache = new HashMap<String, Integer>();

    private static HashMap<String, CompareValues> comparators = null;

    public static List<ResultDescriptor> getResultDescriptors(SimulationRun run) {
        List<ResultDescriptor> result = []
        File file = new File(getSimRunPath(run))
        for (File f in file.listFiles()) {
            String[] ids = f.name.split("_")
            result.add(new ResultDescriptor(Long.parseLong(ids[0]), Long.parseLong(ids[2]), Long.parseLong(ids[3]), Integer.parseInt(ids[1])))
        }

        return result
    }

    static List getRawData(SimulationRun simulationRun) {
        //TODO: won't work like this
        return SingleValueResult.findAllBySimulationRun(simulationRun)
    }

    static List getPaths(SimulationRun simulationRun) {
        File simRun = new File(getSimRunPath(simulationRun));
        if (simRun.listFiles().length == 0) {
            return []
        }

        String pathIds = "(";
        for (File f: simRun.listFiles()) {
            pathIds += f.getName().split("_")[0] + ",";
        }
        pathIds = pathIds.substring(0, pathIds.length() - 1) + ")";

        return SingleValueResult.executeQuery("SELECT DISTINCT p.pathName " +
                "FROM org.pillarone.riskanalytics.core.output.PathMapping as p " +
                "WHERE p.id IN " + pathIds)
    }


    static Double getMean(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        int pathId = getPathId(pathName, simulationRun.id);
        int fieldId = getFieldId(fieldName, simulationRun.id);
        int collectorId = CollectorMapping.findByCollectorName(collectorName).id
        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, pathName, collectorName, fieldName, PostSimulationCalculation.MEAN)

        if (result != null) {
            return result.result
        } else {

            File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, pathId, fieldId, collectorId, periodIndex))
            double avg = 0;
            double count = 0;

            IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
            while (ifa.fetchNext()) {
                avg += ifa.getValue();
                count++;
            }

            return avg / count;
        }
    }


    static Double getMin(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        int pathId = getPathId(pathName, simulationRun.id);
        int fieldId = getFieldId(fieldName, simulationRun.id);
        int collectorId = CollectorMapping.findByCollectorName(collectorName).id
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, pathId, fieldId, collectorId, periodIndex))
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        double min = Double.MAX_VALUE;
        while (ifa.fetchNext()) {
            min = Math.min(min, ifa.getValue());
        }
        return min;
    }

    static Double getMax(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        int pathId = getPathId(pathName, simulationRun.id);
        int fieldId = getFieldId(fieldName, simulationRun.id);
        int collectorId = CollectorMapping.findByCollectorName(collectorName).id
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, pathId, fieldId, collectorId, periodIndex))
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        double max = Double.MIN_VALUE;
        while (ifa.fetchNext()) {
            max = Math.max(max, ifa.getValue());
        }
        return max;
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
        return getValuesSorted(simulationRun, periodIndex, getPathId(pathName, simulationRun.id), CollectorMapping.findByCollectorName(collectorName).id, getFieldId(fieldName, simulationRun.id));
    }

    static List getValuesSorted(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        //delegate to java class -> performance improvement in PSC
        return IterationFileAccessor.getValuesSorted(simulationRun.id, period, pathId, collectorId, fieldId)
    }

    static List getValues(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        return getValues(simulationRun, periodIndex, getPathId(pathName, simulationRun.id), CollectorMapping.findByCollectorName(collectorName).id, getFieldId(fieldName, simulationRun.id));
    }

    static List getValues(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, pathId, fieldId, collectorId, period))
        HashMap<Integer, Double> tmpValues = new HashMap<Integer, Double>(10000);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        while (ifa.fetchNext()) {
            tmpValues.put(ifa.getIteration(), ifa.getValue());
        }
        List<Double> values = new ArrayList<Double>(tmpValues.size());
        for (int i = 0; i <= tmpValues.size(); i++) {
            Double d = null
            if ((d = tmpValues.get(i)) != null) {
                values.add(d);
            }
        }

        return values;
    }



    public static List<Object[]> getAvgAndIsStochasticForSimulationRun(SimulationRun simulationRun) {
        /*Sql sql = new Sql(simulationRun.dataSource)
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
        return result*/
        return getAvgAndIsStochastic(simulationRun);
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
        /*PathMapping path = PathMapping.findByPathName(pathName)
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
        return res[0][1]*/
        return getSingleIterationValue(simulationRun, periodIndex, pathName, fieldName, iteration);
    }

    private static String getSimRunPath(SimulationRun simulationRun) {
        return GridHelper.getResultLocation(simulationRun.id)
    }

    public static List<Object[]> getAvgAndIsStochastic(SimulationRun simulationRun) {
        File simRun = new File(getSimRunPath(simulationRun));
        def result = []
        for (File f: simRun.listFiles()) {
            def array = new Object[7]
            IterationFileAccessor ifa = new IterationFileAccessor(f);
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY, avg = 0;
            double count = 0;
            while (ifa.fetchNext()) {
                min = Math.min(ifa.getValue(), min);
                max = Math.max(ifa.getValue(), max);
                avg += ifa.getValue();
                count++;
            }

            avg = avg / count;
            String[] path_period_field = f.getName().split("_");
            for (int i = 0; i < 2; i++) {
                array[i] = Long.parseLong(path_period_field[i]);
            }
            array[2] = CollectorMapping.findByCollectorName(AggregatedCollectingModeStrategy.IDENTIFIER).id //TODO: replace with correct collector id
            array[3] = Long.parseLong(path_period_field[2]);
            array[4] = avg;
            array[5] = min;
            array[6] = max;
            result << array;
        }

        return result;
    }

    private static int getPathId(String pathName, long simId) {
        Integer pathId = null;
        if ((pathId = pathCache.get(pathName + simId)) == null) {
            def res = SingleValueResult.executeQuery("SELECT DISTINCT p.id " +
                    "FROM org.pillarone.riskanalytics.core.output.PathMapping as p " +
                    "WHERE p.pathName = ?", [pathName]);

            pathId = new Integer((int) res[0]);
            pathCache.put(pathName + simId, pathId)
        }
        return pathId;
    }
//

    private static int getFieldId(String fieldName, long simId) {
        Integer fieldId = null;
        if ((fieldId = pathCache.get(fieldName + simId)) == null) {
            def res = SingleValueResult.executeQuery("SELECT DISTINCT f.id " +
                    "FROM org.pillarone.riskanalytics.core.output.FieldMapping as f " +
                    "WHERE f.fieldName = ?", [fieldName]);
            fieldId = new Integer((int) res[0]);
            pathCache.put(fieldName + simId, fieldId)
        }
        return fieldId;
    }

    public static Double getSingleIterationValue(SimulationRun simulationRun, int period, String path, String field, int iteration) {
        File iterationFile = new File(getSimRunPath(simulationRun) + File.separator + getPathId(path, simulationRun.id)
                + "_" + period + "_" + getFieldId(field, simulationRun.id));
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);

        while (ifa.fetchNext()) {
            if (ifa.getIteration() == iteration)
                return new Double(ifa.getValue());
        }
        return null;
    }

    public static synchronized void initComparators() {

        if (comparators != null) return;

        comparators = new HashMap<String, CompareValues>();

        comparators.put("<", new CompareValues() {
            public boolean compareValues(double d1, double d2) {
                if (d1 < d2)
                    return true;
                return false;
            }
        });
        comparators.put("<=", new CompareValues() {
            public boolean compareValues(double d1, double d2) {
                if (d1 <= d2)
                    return true;
                return false;
            }
        });
        comparators.put("=", new CompareValues() {
            public boolean compareValues(double d1, double d2) {
                if (d1 == d2)
                    return true;
                return false;
            }
        });
        comparators.put(">=", new CompareValues() {
            public boolean compareValues(double d1, double d2) {
                if (d1 >= d2)
                    return true;
                return false;
            }
        });

        comparators.put(">", new CompareValues() {
            public boolean compareValues(double d1, double d2) {
                if (d1 > d2)
                    return true;
                return false;
            }
        });
    }

    public static List getCriteriaConstrainedIterations(SimulationRun simulationRun, int period, String path, String field, String collector,
                                                        String criteria, double conditionValue) {
        initComparators();
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, getPathId(path, simulationRun.id), getFieldId(field, simulationRun.id), CollectorMapping.findByCollectorName(collector).id, period))
        HashMap<Integer, Double> tmpValues = new HashMap<Integer, Double>(10000);
        List<Integer> iterations = new ArrayList<Integer>();
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);

        while (ifa.fetchNext()) {
            tmpValues.put(ifa.getIteration(), ifa.getValue());
        }
        CompareValues currentComparator = comparators.get(criteria);
        if (currentComparator != null) {

            for (int i = 1; i <= tmpValues.size(); i++) {
                if (currentComparator.compareValues(tmpValues.get(i), conditionValue))
                    iterations.add(i);
            }
        }
        return iterations;
    }

    public static List getIterationConstrainedValues(SimulationRun simulationRun, int period, String path, String field, String collector,
                                                     List<Integer> iterations) {
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, getPathId(path, simulationRun.id), getFieldId(field, simulationRun.id), CollectorMapping.findByCollectorName(collector).id, period))
        HashMap<Integer, Double> tmpValues = new HashMap<Integer, Double>(10000);
        List<Double> values = new ArrayList<Double>();
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        Collections.sort(iterations);

        while (ifa.fetchNext()) {
            tmpValues.put(ifa.getIteration(), ifa.getValue());
        }

        for (int i: iterations) {
            Double d = null;
            if ((d = tmpValues.get(i)) != null)
                values.add(d)
        }
        return values;
    }

    public static List getSingleValueResults(String collector, String path, String field, SimulationRun run) {
        List result = []
        long pathId = getPathId(path, run.id)
        long fieldId = getFieldId(field, run.id)
        long collectorId = CollectorMapping.findByCollectorName(collector).id
        for (int i = 0; i < run.periodCount; i++) {
            File f = new File(GridHelper.getResultPathLocation(run.id, pathId, fieldId, collectorId, i))
            IterationFileAccessor ifa = new IterationFileAccessor(f)
            int index = 0
            while (ifa.fetchNext()) {
                int iteration = ifa.iteration
                List<Double> values = ifa.singleValues
                for (Double val in values) {
                    result << [path, val, field, iteration, i, index++] as Object[]
                }
            }
        }
        return result
    }

}

interface CompareValues {
    public boolean compareValues(double d1, double d2)
}