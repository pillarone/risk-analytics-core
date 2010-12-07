package org.pillarone.riskanalytics.core.dataaccess

import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.output.*

import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper

class ResultAccessor {

    private static HashMap<String, Integer> pathCache = new HashMap<String, Integer>();
    private static HashMap<String, Integer> fieldCache = new HashMap<String, Integer>();

    private static HashMap<String, CompareValues> comparators = null;

    static List getRawData(SimulationRun simulationRun) {

        return SingleValueResult.findAllBySimulationRun(simulationRun)
    }

    static List getPaths(SimulationRun simulationRun) {
        return getPathsNew(simulationRun);
        /*return SingleValueResult.executeQuery("SELECT DISTINCT p.pathName " +
                "FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s, org.pillarone.riskanalytics.core.output.PathMapping as p " +
                "WHERE s.path.id = p.id AND s.simulationRun.id = " + simulationRun.id)*/
    }


    static Double getMean(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {

        return getMeanNew(simulationRun, periodIndex, pathName, collectorName, fieldName);
        /*if (simulationRun.iterations == 1) {
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
        }*/
    }


    static Double getMin(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        return getMinNew(simulationRun, periodIndex, pathName, collectorName, fieldName);
        /*def res = SingleValueResult.executeQuery("SELECT MIN(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        return res[0]*/
    }

    static Double getMax(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        return getMaxNew(simulationRun, periodIndex, pathName, collectorName, fieldName);
        /*def res = SingleValueResult.executeQuery("SELECT MAX(value) FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.period = ? AND " +
                "s.simulationRun.id = ?", [pathName, collectorName, fieldName, periodIndex, simulationRun.id])
        return res[0]*/
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
        if ((compareOperator.equals(CompareOperator.LESS_THAN) && percentage == 0)
                || compareOperator.equals(CompareOperator.GREATER_THAN) && percentage == 100) {
            return null
        }
        double[] values = getValuesSorted(simulationRun, periodIndex, path, collectorName, fieldName) as double[]
        Double rank =  simulationRun.getIterations() * percentage * 0.01
        if (rank > 0) {
            rank--        // -1 as array index starts with 0
        }
        Integer index = rank.toInteger()
        if (Math.abs(rank - index) > 0 && compareOperator.equals(CompareOperator.GREATER_EQUALS)
            || compareOperator.equals(CompareOperator.GREATER_THAN)) {
            index++
        }
        return values[index]
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
        /*SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.period = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.simulationRun.id = ? ORDER BY value", [pathName, periodIndex, collectorName, fieldName, simulationRun.id])*/
        return getValuesSortedNew(simulationRun, periodIndex, pathName, collectorName, fieldName);
    }

    static List getValuesSorted(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        return getValuesSortedNew(simulationRun, period, pathId, collectorId, fieldId);
        /*SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.id = ? AND " +
                "s.period = ? AND " +
                "s.collector.id = ? AND " +
                "s.field.id = ? AND " +
                "s.simulationRun.id = ? ORDER BY value", [pathId, period, collectorId, fieldId, simulationRun.id])*/
    }

    static List getValues(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        return getValuesNew(simulationRun, periodIndex, pathName, collectorName, fieldName);
        /*SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.pathName = ? AND " +
                "s.period = ? AND " +
                "s.collector.collectorName = ? AND " +
                "s.field.fieldName = ? AND " +
                "s.simulationRun.id = ? ORDER BY s.iteration", [pathName, periodIndex, collectorName, fieldName, simulationRun.id])*/
    }

    static List getValues(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        return getValuesNew(simulationRun, period, pathId, collectorId, fieldId);
        /*SingleValueResult.executeQuery("SELECT value FROM org.pillarone.riskanalytics.core.output.SingleValueResult as s " +
                " WHERE s.path.id = ? AND " +
                "s.period = ? AND " +
                "s.collector.id = ? AND " +
                "s.field.id = ? AND " +
                "s.simulationRun.id = ? ORDER BY s.iteration", [pathId, period, collectorId, fieldId, simulationRun.id])*/
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


    private static List getPathsNew(SimulationRun simulationRun) {
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

    private static Double getMeanNew(SimulationRun simulationRun, int periodIndex = 0, String pathName,
                                     String collectorName, String fieldName) {
        long tstamp = System.currentTimeMillis();
        int pathId = getPathId(pathName, simulationRun.id);
        int fieldId = getFieldId(fieldName, simulationRun.id);

        def result = PostSimulationCalculationAccessor.getResult(simulationRun, periodIndex, pathName, collectorName, fieldName, PostSimulationCalculation.MEAN)

        if (result != null) {
            return result.result
        } else {

            File iterationFile = new File(getSimRunPath(simulationRun) + File.separator + pathId + "_" + periodIndex + "_" + fieldId);
            double avg = 0;
            double count = 0;

            IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
            while (ifa.fetchNext()) {
                avg += ifa.getValue();
                count++;
            }

            tstamp = System.currentTimeMillis() - tstamp;
            return avg / count;
        }
    }

    static Double getMinNew(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        int pathId = getPathId(pathName, simulationRun.id);
        int fieldId = getFieldId(fieldName, simulationRun.id);
        File iterationFile = new File(getSimRunPath(simulationRun) + File.separator + pathId + "_" + periodIndex + "_" + fieldId);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        double min = Double.MAX_VALUE;
        while (ifa.fetchNext()) {
            min = Math.min(min, ifa.getValue());
        }
        return min;
    }

    static Double getMaxNew(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        int pathId = getPathId(pathName, simulationRun.id);
        int fieldId = getFieldId(fieldName, simulationRun.id);
        File iterationFile = new File(getSimRunPath(simulationRun) + File.separator + pathId + "_" + periodIndex + "_" + fieldId);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        double max = 0;
        while (ifa.fetchNext()) {
            max = Math.max(max, ifa.getValue());
        }
        return max;
    }

    public static List getValuesNew(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {

        File iterationFile = new File(getSimRunPath(simulationRun) + File.separator + pathId + "_" + period + "_" + fieldId);
        HashMap<Integer, Double> tmpValues = new HashMap<Integer, Double>(10000);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        while (ifa.fetchNext()) {
            tmpValues.put(ifa.getId(), ifa.getValue());
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
//

    public static List getValuesNew(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        return getValuesNew(simulationRun, periodIndex, getPathId(pathName, simulationRun.id), 0, getFieldId(fieldName, simulationRun.id));
    }

    public static List getValuesSortedNew(SimulationRun simulationRun, int period, long pathId, long collectorId, long fieldId) {
        File iterationFile = new File(getSimRunPath(simulationRun) + File.separator + pathId + "_" + period + "_" + fieldId);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        List<Double> values = new ArrayList<Double>();
        while (ifa.fetchNext()) {
            values.add(ifa.getValue());
        }
        Collections.sort(values);
        return values;
    }

    public static List getValuesSortedNew(SimulationRun simulationRun, int periodIndex = 0, String pathName, String collectorName, String fieldName) {
        return getValuesSortedNew(simulationRun, periodIndex, getPathId(pathName, simulationRun.id), 0, getFieldId(fieldName, simulationRun.id));
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

    public static List getCriteriaConstrainedIterations(SimulationRun simulationRun, int period, String path, String field,
                                                        String criteria, double conditionValue) {
        initComparators();
        File iterationFile = new File(getSimRunPath(simulationRun) + File.separator + getPathId(path, simulationRun.id)
                + "_" + period + "_" + getFieldId(field, simulationRun.id));
        HashMap<Integer, Double> tmpValues = new HashMap<Integer, Double>(10000);
        List<Integer> iterations = new ArrayList<Integer>();
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);

        while (ifa.fetchNext()) {
            tmpValues.put(ifa.getId(), ifa.getValue());
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
//
    public static List getIterationConstrainedValues(SimulationRun simulationRun, int period, String path, String field,
                                                     List<Integer> iterations) {
        File iterationFile = new File(getSimRunPath(simulationRun) + File.separator + getPathId(path, simulationRun.id)
                + "_" + period + "_" + getFieldId(field, simulationRun.id));
        HashMap<Integer, Double> tmpValues = new HashMap<Integer, Double>(10000);
        List<Double> values = new ArrayList<Double>();
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        Collections.sort(iterations);

        while (ifa.fetchNext()) {
            tmpValues.put(ifa.getId(), ifa.getValue());
        }

        for (int i: iterations) {
            Double d = null;
            if ((d = tmpValues.get(i)) != null)
                values.add(d)
        }
        return values;
    }

}


class IterationFileAccessor {
    DataInputStream dis;
    int id;
    double value;

    public IterationFileAccessor(File f) {

        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bs = new BufferedInputStream(fis);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] b = new byte[8048];
        int len;
        int count = 0;
        while ((len = bs.read(b)) != -1) {
            bos.write(b, 0, len);
            count++;
        }
        if (count == 0) {
            Thread.sleep(2000)
            while ((len = bs.read(b)) != -1) {
                bos.write(b, 0, len);
                count++;
            }
        }
        bs.close();
        fis.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        dis = new DataInputStream(bis);

    }

    public boolean fetchNext() {
        if (dis.available() > 4) {
            id = dis.readInt();
            int len = dis.readInt();
            value = 0;
            for (int i = 0; i < len; i++){
                value += dis.readDouble();
                dis.readLong();
            }

            return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public double getValue() {
        return value;
    }
}

interface CompareValues {
    public boolean compareValues(double d1, double d2)

    ;
}