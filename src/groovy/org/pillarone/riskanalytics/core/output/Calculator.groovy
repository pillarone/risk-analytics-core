package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.jfree.data.statistics.HistogramDataset
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.output.batch.calculations.AbstractCalculationsBulkInsert
import org.pillarone.riskanalytics.core.util.MathUtils

class Calculator {

    private static Log LOG = LogFactory.getLog(Calculator)

    private SimulationRun run
    private List paths
    private int totalCalculations
    private int completedCalculations
    private Map keyFigures
    private long startTime
    int keyFigureCount

    private AbstractCalculationsBulkInsert bulkInsert

    boolean stopped = false

    public Calculator(SimulationRun run) {
        bulkInsert = AbstractCalculationsBulkInsert.getBulkInsertInstance()
        bulkInsert.simulationRun = run
        this.run = run
        paths = ResultAccessor.getPaths(run)
        keyFigures = ApplicationHolder.application.config.keyFiguresToCalculate
        keyFigureCount = 0 //isStochastic + mean
        keyFigures.entrySet().each {Map.Entry entry ->
            if (entry.value instanceof List) {
                keyFigureCount += entry.value.size()
            } else {
                keyFigureCount++
            }
        }
        totalCalculations = keyFigureCount * paths.size() * run.periodCount
        completedCalculations = 0
    }

    int getProgress() {
        return (int) ((double) completedCalculations / (double) totalCalculations * 100d)
    }

    Date getEstimatedEnd() {
        if (startTime == null || completedCalculations == 0) {
            return null
        }

        long now = System.currentTimeMillis()
        long timeForOneKeyFigure = (now - startTime) / completedCalculations
        return new Date(now + (totalCalculations - completedCalculations) * timeForOneKeyFigure)
    }

    void calculate() {
        long singleCollectorId = -1
        CollectorMapping collectorMapping = CollectorMapping.findByCollectorName(SingleValueCollectingModeStrategy.IDENTIFIER)
        if (collectorMapping)
            singleCollectorId = collectorMapping?.id

        startTime = System.currentTimeMillis()
        List<Object[]> result = ResultAccessor.getAvgAndIsStochasticForSimulationRun(run, singleCollectorId)
//        totalCalculations = keyFigureCount * ResultAccessor.getAvgAndIsStochasticForSimulationRunCount(run)
        totalCalculations = keyFigureCount * ResultAccessor.getAvgAndIsStochasticForSimulationRunCount(result)

        for (Object[] array in result) {
            long path = array[0]
            int periodIndex = array[1]
            long collector = array[2]
            long field = array[3]
            double avg = array[4]
            //use only aggregated values
            if (collector == collectorMapping?.id) {
//                println "${collector}"
//                continue
            }

            int isStochastic = array[5] == array[6] ? 1 : 0
            bulkInsert.addResults(periodIndex, PostSimulationCalculation.MEAN, null, path, field, collector, avg)
            bulkInsert.addResults(periodIndex, PostSimulationCalculation.IS_STOCHASTIC, null, path, field, collector, isStochastic)

            if (isStochastic == 0) {
                double[] values = loadValues(path, periodIndex, collector, field, singleCollectorId)
                if (keyFigures.get(PostSimulationCalculation.STDEV)) {
                    calculateStandardDeviation(periodIndex, path, collector, field, values, avg)
                    completedCalculations++
                }
                def percentiles = keyFigures.get(PostSimulationCalculation.PERCENTILE)
                def vars = keyFigures.get(PostSimulationCalculation.VAR)
                def tvars = keyFigures.get(PostSimulationCalculation.TVAR)
                def pdf = keyFigures.get(PostSimulationCalculation.PDF)
                percentiles?.each {double p ->
                    calculatePercentile(periodIndex, path, collector, field, values, p)
                    completedCalculations++
                }
                vars?.each {double p ->
                    calculateVar(periodIndex, path, collector, field, values, p, avg)
                    completedCalculations++
                }
                tvars?.each {double p ->
                    calculateTvar(periodIndex, path, collector, field, values, p)
                    completedCalculations++
                }
                if (pdf) {
                    calculatePDF(periodIndex, path, collector, field, values, pdf)
                }
            }
        }
        bulkInsert.saveToDB()
        LOG.info("Post Simulation Calculation done in ${System.currentTimeMillis() - startTime}ms (#paths ${paths.size()})")
    }

    /**
     *  Values of path and periodIndex are loaded from the database and returned in a sorted double[]
     */
    private double[] loadValues(long pathId, int periodIndex, long collector, long fieldId, long singleCollectorId = -1) {

        long time = System.currentTimeMillis()

        double[] results = ResultAccessor.getValuesSorted(run, periodIndex, pathId, collector, fieldId, singleCollectorId) as double[]

        LOG.debug("Loaded results for calculations ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
        return results
    }


    private void calculateStandardDeviation(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double mean) {
        long time = System.currentTimeMillis()

        Double stdev = MathUtils.calculateStandardDeviation(results, mean)
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.STDEV, null, pathId, fieldId, collectorId, stdev)

        LOG.debug("Calculated stdev ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculatePercentile(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double percentile) {
        long time = System.currentTimeMillis()

        BigDecimal p = MathUtils.calculatePercentileOfSortedValues(results, percentile)
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.PERCENTILE, percentile, pathId, fieldId, collectorId, p)


        LOG.debug("Calculated percentile $percentile ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculatePDF(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, def pdf) {
        long time = System.currentTimeMillis()
        HistogramDataset data = new KiloHistogramDataset()
        Map pdfData = data.createPdfData(results, run, pdf)
        pdfData.each {BigDecimal k, v ->
            bulkInsert.addResults(periodIndex, PostSimulationCalculation.PDF, k, pathId, fieldId, collectorId, v)
        }

        LOG.debug("Calculated pdf $pdf ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }


    private void calculateVar(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double percentile, double mean) {
        long time = System.currentTimeMillis()

        BigDecimal var = MathUtils.calculateVarOfSortedValues(results, percentile, mean)
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.VAR, percentile, pathId, fieldId, collectorId, var)


        LOG.debug("Calculated var $percentile ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculateTvar(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double percentile) {
        long time = System.currentTimeMillis()

        BigDecimal tvar = MathUtils.calculateTvarOfSortedValues(results, percentile)
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.TVAR, percentile, pathId, fieldId, collectorId, tvar)


        LOG.debug("Calculated tvar $percentile ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }


}