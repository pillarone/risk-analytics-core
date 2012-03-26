package org.pillarone.riskanalytics.core.output

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.output.batch.calculations.AbstractCalculationsBulkInsert
import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.dataaccess.ResultDescriptor
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.dataaccess.ResultPathDescriptor

class Calculator {

    private static Log LOG = LogFactory.getLog(Calculator)

    private SimulationRun run
    private List<ResultPathDescriptor> pathDescriptors
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
        pathDescriptors = ResultAccessor.getDistinctPaths(run)
        keyFigures = ApplicationHolder.application.config.keyFiguresToCalculate
        keyFigureCount = 2 //isStochastic + mean
        keyFigures.entrySet().each {Map.Entry entry ->
            if (entry.value instanceof List) {
                keyFigureCount += entry.value.size()
            }
            else {
                keyFigureCount++
            }
        }
        totalCalculations = keyFigureCount * pathDescriptors.size()
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
        startTime = System.currentTimeMillis()

        for (ResultPathDescriptor descriptor in pathDescriptors) {
            PathMapping path = descriptor.path
            int periodIndex = descriptor.period
            CollectorMapping collector = descriptor.collector
            FieldMapping field = descriptor.field

            double[] values = loadValues(path, periodIndex, collector, field)
            double avg = calculateMean(periodIndex, path, collector, field, values)
            boolean isStochastic = calculateIsStochastic(periodIndex, path, collector, field, values)

            if (isStochastic) {
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
            } else {
                totalCalculations -= (keyFigureCount - 2)
            }
        }
        bulkInsert.saveToDB()
        LOG.info("Post Simulation Calculation done in ${System.currentTimeMillis() - startTime}ms (#paths ${pathDescriptors.size()})")
    }

    /**
     *  Values of path and periodIndex are loaded from the database and returned in a sorted double[]
     */
    private double[] loadValues(PathMapping path, int periodIndex, CollectorMapping collector, FieldMapping field) {

        long time = System.currentTimeMillis()

        double[] results = ResultAccessor.getValuesSorted(run, periodIndex, path.pathName, collector.collectorName, field.fieldName) as double[]

        LOG.debug("Loaded results for calculations (${path.pathName}, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
        return results
    }


    private double calculateMean(int periodIndex, long pathId, long collectorId, long fieldId, double[] results) {
        long time = System.currentTimeMillis()

        double mean = results.toList().sum() / results.size()
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.MEAN, null, pathId, fieldId, collectorId, mean)

        LOG.debug("Calculated mean ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
        return mean
    }

    private boolean calculateIsStochastic(int periodIndex, long pathId, long collectorId, long fieldId, double[] results) {
        long time = System.currentTimeMillis()

        boolean isStochastic = results[0] == results[-1]
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.IS_STOCHASTIC, null, pathId, fieldId, collectorId, isStochastic ? 1 : 0)

        LOG.debug("Calculated is stochastic ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
        return isStochastic
    }

    private double calculateMean(int periodIndex, PathMapping path, CollectorMapping collector, FieldMapping field, double[] results) {
        long time = System.currentTimeMillis()

        Double mean = MathUtils.calculateMean(results)
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.MEAN, null, path.id, field.id, collector.id, mean)

        LOG.debug("Calculated mean ($path.pathName, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")

        return mean
    }


    private boolean calculateIsStochastic(int periodIndex, PathMapping path, CollectorMapping collector, FieldMapping field, double[] results) {
        long time = System.currentTimeMillis()

        boolean isStochastic = results[0] != results[results.length - 1]
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.IS_STOCHASTIC, null, path.id, field.id, collector.id, isStochastic ? 0 : 1)

        LOG.debug("Calculated is stochastic ($path.pathName, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")

        return isStochastic
    }


    private void calculateStandardDeviation(int periodIndex, PathMapping path, CollectorMapping collector, FieldMapping field, double[] results, double mean) {
        long time = System.currentTimeMillis()

        Double stdev = MathUtils.calculateStandardDeviation(results, mean)
        bulkInsert.addResults(periodIndex, PostSimulationCalculation.STDEV, null, path.id, field.id, collector.id, stdev)

        LOG.debug("Calculated stdev (${path.pathName}, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculatePercentile(int periodIndex, PathMapping path, CollectorMapping collector, FieldMapping field, double[] results, double severity, QuantilePerspective perspective) {
        long time = System.currentTimeMillis()

        BigDecimal p = MathUtils.calculatePercentileOfSortedValues(results, severity, perspective)
        bulkInsert.addResults(periodIndex, perspective.getPercentileAsString(), severity, path.id, field.id, collector.id, p)


        LOG.debug("Calculated percentile $severity (${path.pathName}, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculatePDF(int periodIndex, PathMapping path, CollectorMapping collector, FieldMapping field, double[] results, def pdf) {
        long time = System.currentTimeMillis()
        PdfFromSample data = new PdfFromSample()
        Map pdfData = data.createPdfData(results, pdf)
        pdfData.each {BigDecimal k, v ->
            bulkInsert.addResults(periodIndex, PostSimulationCalculation.PDF, k, path.id, field.id, collector.id, v)
        }

        LOG.debug("Calculated pdf $pdf (${path.pathName}, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }


    private void calculateVar(int periodIndex, PathMapping path, CollectorMapping collector, FieldMapping field, double[] results, double severity, double mean,
                              QuantilePerspective perspective) {
        long time = System.currentTimeMillis()

        BigDecimal var = MathUtils.calculateVarOfSortedValues(results, severity, mean, perspective)
        bulkInsert.addResults(periodIndex, perspective.getVarAsString(), severity, path.id, field.id, collector.id, var)


        LOG.debug("Calculated var $severity (${path.pathName}, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculateTvar(int periodIndex, PathMapping path, CollectorMapping collector, FieldMapping field, double[] results, double severity,
                               QuantilePerspective perspective) {
        long time = System.currentTimeMillis()

        BigDecimal tvar = MathUtils.calculateTvarOfSortedValues(results, severity, perspective)
        bulkInsert.addResults(periodIndex, perspective.getTvarAsString(), severity, path.id, field.id, collector.id, tvar)


        LOG.debug("Calculated tvar $severity (${path.pathName}, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

}