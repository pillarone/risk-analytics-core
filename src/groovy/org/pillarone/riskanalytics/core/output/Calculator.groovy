package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.output.batch.calculations.AbstractCalculationsBulkInsert
import org.pillarone.riskanalytics.core.util.MathUtils
import org.joda.time.DateTime

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
            }
            else {
                keyFigureCount++
            }
        }
        totalCalculations = keyFigureCount * paths.size() * run.periodCount
        completedCalculations = 0
    }

    int getProgress() {
        return (int) ((double) completedCalculations / (double) totalCalculations * 100d)
    }

    DateTime getEstimatedEnd() {
        if (startTime == null || completedCalculations == 0) {
            return null
        }

        long now = System.currentTimeMillis()
        long timeForOneKeyFigure = (now - startTime) / completedCalculations
        return new DateTime(now + (totalCalculations - completedCalculations) * timeForOneKeyFigure)
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
                def percentilesProfit = keyFigures.get(PostSimulationCalculation.PERCENTILE_PROFIT)
                def varsProfit = keyFigures.get(PostSimulationCalculation.VAR_PROFIT)
                def tvarsProfit = keyFigures.get(PostSimulationCalculation.TVAR_PROFIT)
                def pdf = keyFigures.get(PostSimulationCalculation.PDF)
                percentiles?.each {double p ->
                    calculatePercentile(periodIndex, path, collector, field, values, p, QuantilePerspective.LOSS)
                    completedCalculations++
                }
                vars?.each {double p ->
                    calculateVar(periodIndex, path, collector, field, values, p, avg, QuantilePerspective.LOSS)
                    completedCalculations++
                }
                tvars?.each {double p ->
                    calculateTvar(periodIndex, path, collector, field, values, p, QuantilePerspective.LOSS)
                    completedCalculations++
                }
                percentilesProfit?.each {double p ->
                    calculatePercentile(periodIndex, path, collector, field, values, p, QuantilePerspective.PROFIT)
                    completedCalculations++
                }
                varsProfit?.each {double p ->
                    calculateVar(periodIndex, path, collector, field, values, p, avg, QuantilePerspective.PROFIT)
                    completedCalculations++
                }
                tvarsProfit?.each {double p ->
                    calculateTvar(periodIndex, path, collector, field, values, p, QuantilePerspective.PROFIT)
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

    private void calculatePercentile(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double severity, QuantilePerspective perspective) {
        long time = System.currentTimeMillis()

        BigDecimal p = MathUtils.calculatePercentileOfSortedValues(results, severity, perspective)
        bulkInsert.addResults(periodIndex, perspective.getPercentileAsString(), severity, pathId, fieldId, collectorId, p)


        LOG.debug("Calculated percentile $severity ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculatePDF(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, def pdf) {
        long time = System.currentTimeMillis()
        PdfFromSample data = new PdfFromSample()
        Map pdfData = data.createPdfData(results, pdf)
        pdfData.each {BigDecimal k, v ->
            bulkInsert.addResults(periodIndex, PostSimulationCalculation.PDF, k, pathId, fieldId, collectorId, v)
        }

        LOG.debug("Calculated pdf $pdf ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }


    private void calculateVar(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double severity, double mean,
                              QuantilePerspective perspective) {
        long time = System.currentTimeMillis()

        BigDecimal var = MathUtils.calculateVarOfSortedValues(results, severity, mean, perspective)
        bulkInsert.addResults(periodIndex, perspective.getVarAsString(), severity, pathId, fieldId, collectorId, var)


        LOG.debug("Calculated var $severity ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculateTvar(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double severity,
                               QuantilePerspective perspective) {
        long time = System.currentTimeMillis()

        BigDecimal tvar = MathUtils.calculateTvarOfSortedValues(results, severity, perspective)
        bulkInsert.addResults(periodIndex, perspective.getTvarAsString(), severity, pathId, fieldId, collectorId, tvar)


        LOG.debug("Calculated tvar $severity ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

}