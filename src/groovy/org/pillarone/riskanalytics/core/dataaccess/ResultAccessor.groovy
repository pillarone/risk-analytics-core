package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.output.*
import groovy.sql.Sql
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

abstract class ResultAccessor {

    private static final Log LOG = LogFactory.getLog(ResultAccessor)

    private static HashMap<String, Integer> pathCache = new HashMap<String, Integer>();
    private static HashMap<String, Integer> fieldCache = new HashMap<String, Integer>();
    private static HashMap<String, Integer> collectorCache = new HashMap<String, Integer>();

    private static HashMap<String, CompareValues> comparators = null;

    static List<SingleValueResultPOJO> getAllResults(SimulationRun simulationRun) {
        List<ResultPathDescriptor> paths = getDistinctPaths(simulationRun)
        List<SingleValueResultPOJO> result = []

        for(ResultPathDescriptor descriptor in paths) {
            double[] values = getValues(simulationRun, descriptor.period, descriptor.path.pathName, descriptor.collector.collectorName, descriptor.field.fieldName)
            for(double value in values) {
                result << new SingleValueResultPOJO(
                        path: descriptor.path, field: descriptor.field, collector: descriptor.collector,
                        period: descriptor.period, simulationRun: simulationRun, value: value
                )
            }

        }
        return result
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

    static List<ResultPathDescriptor> getDistinctPaths(SimulationRun run) {
        CollectorMapping singleCollector = CollectorMapping.findByCollectorName(SingleValueCollectingModeStrategy.IDENTIFIER)
        if (singleCollector == null) {
            throw new IllegalStateException("Single collector mapping not found")
        }

        List<ResultPathDescriptor> result = []
        File file = new File(GridHelper.getResultLocation(run.id))
        for (File f in file.listFiles()) {
            String[] ids = f.name.split("_")
            long collectorId = Long.parseLong(ids[3])
            if (collectorId != singleCollector.id) {
                result.add(new ResultPathDescriptor(PathMapping.get(Long.parseLong(ids[0])), FieldMapping.get(Long.parseLong(ids[2])), CollectorMapping.get(collectorId), Integer.parseInt(ids[1])))
            }
        }

        return result
    }

    static Double getMean(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        PostSimulationCalculation result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, pathName, collectorName, fieldName, PostSimulationCalculation.MEAN)
        if (result != null) {
            return result.result
        } else {
            List<Double> allValues = getValues(simulationRun, periodIndex,pathName, collectorName, fieldName)
            return allValues.sum() / simulationRun.iterations
        }
    }

    static Double getMin(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        double[] sortedValues = getValuesSorted(simulationRun, periodIndex, pathName, collectorName, fieldName)
        if(sortedValues.length == 0) {
            return null
        }
        return sortedValues[0]
    }

    static Double getMax(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        double[] sortedValues = getValuesSorted(simulationRun, periodIndex, pathName, collectorName, fieldName)
        if(sortedValues.length == 0) {
            return null
        }
        return sortedValues[-1]
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
        } else {
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
        } else {
            double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
            if (values.length == 0) {
                return null
            }
            return MathUtils.calculateTvar(values, severity, perspective)
        }
    }

    static double[] getValuesSorted(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        //delegate to java class -> performance improvement in PSC
        return fillWithZeroes(simulationRun, (double[]) IterationFileAccessor.getValuesSorted(simulationRun.id, periodIndex, getPathId(pathName), getCollectorId(collectorName), getFieldId(fieldName)))
    }

    static double[] getValues(SimulationRun simulationRun, int periodIndex, String pathName, String collectorName, String fieldName) {
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, getPathId(pathName), getFieldId(fieldName), getCollectorId(collectorName), periodIndex))
        HashMap<Integer, Double> tmpValues = new HashMap<Integer, Double>(simulationRun.iterations);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        double[] values = new double[simulationRun.iterations]
        int current = 0
        while (ifa.fetchNext()) {
            values[current++] = ifa.getValue()
        }
        return fillWithZeroes(simulationRun, values);
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
        return getSingleIterationValue(simulationRun, periodIndex, pathName, fieldName, collectorName, iteration)
    }

    private static double[] fillWithZeroes(SimulationRun run, double[] results) {
        if (run.iterations == results.length || results.length == 0) return results

        double[] result = new double[run.iterations]
        System.arraycopy(results, 0, result, 0, results.length)
        for (int i = results.length; i < result.length; i++) {
            result[i] = 0d
        }
        Arrays.sort(result)
        return result
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

    private static int getPathId(String pathName) {
        Integer pathId = pathCache.get(pathName)
        if (pathId == null) {
            pathId = PathMapping.findByPathName(pathName).id
            pathCache.put(pathName, pathId)
        }
        return pathId;
    }

    private static int getFieldId(String fieldName) {
        Integer fieldId = fieldCache.get(fieldName)
        if (fieldId == null) {
            fieldId = FieldMapping.findByFieldName(fieldName).id
            fieldCache.put(fieldName, fieldId)
        }
        return fieldId;
    }

    private static int getCollectorId(String collectorName) {
        Integer collectorId = collectorCache.get(collectorName)
        if (collectorId == null) {
            collectorId = CollectorMapping.findByCollectorName(collectorName).id
            collectorCache.put(collectorName, collectorId)
        }
        return collectorId;
    }

    public static Double getSingleIterationValue(SimulationRun simulationRun, int period, String path, String field, String collector, int iteration) {
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, getPathId(path),getFieldId(field), getCollectorId(collector), period));
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
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, getPathId(path), getFieldId(field), getCollectorId(collector), period))
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

    public static Map<Integer, Double> getIterationConstrainedValues(SimulationRun simulationRun, int period, String path, String field, String collector,
                                                     List<Integer> iterations) {
        File iterationFile = new File(GridHelper.getResultPathLocation(simulationRun.id, getPathId(path), getFieldId(field), getCollectorId(collector), period))
        HashMap<Integer, Double> values = new HashMap<Integer, Double>(10000);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        Collections.sort(iterations);

        while (ifa.fetchNext()) {
            int iteration = ifa.getIteration()
            if (iterations.contains(iteration)) {
                values.put(iteration, ifa.getValue());
            }
        }
        return values;
    }

    public static List getSingleValueResults(String collector, String path, String field, SimulationRun run) {
        List result = []
        long pathId = getPathId(path)
        long fieldId = getFieldId(field)
        long collectorId = getCollectorId(collector)
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

    static boolean isSingleCollector(String collectorName) {
        CollectorMapping collectorMapping = CollectorMapping.findByCollectorName(SingleValueCollectingModeStrategy.IDENTIFIER)
        return collectorName.equals(collectorMapping?.collectorName)
    }

    public static void clearCaches() {
        pathCache.clear()
        fieldCache.clear()
        collectorCache.clear()
    }

}

interface CompareValues {
    public boolean compareValues(double d1, double d2)
}