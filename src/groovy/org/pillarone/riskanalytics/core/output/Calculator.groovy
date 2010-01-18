package org.pillarone.riskanalytics.core.output

import java.sql.ResultSet

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.util.MathUtils
import org.pillarone.riskanalytics.core.output.*
import org.jfree.data.statistics.HistogramDataset

class Calculator {

    private static Log LOG = LogFactory.getLog(Calculator)

    private SimulationRun run
    private List paths
    private int totalCalculations
    private int completedCalculations
    private Map keyFigures
    private long startTime

    boolean stopped = false

    public Calculator(SimulationRun run) {
        this.run = run
        paths = ResultAccessor.getPaths(run)
        keyFigures = ApplicationHolder.application.config.keyFiguresToCalculate
        int keyFigureCount = 2 //isStochastic + mean
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

        startTime = System.currentTimeMillis()

        ResultSet result = ResultAccessor.getAvgAndIsStochasticForSimulationRun(run)

        while (result.next()) {
            long path = result.getLong("path_id")
            int periodIndex = result.getInt("period")
            long collector = result.getInt("collector_id")
            long field = result.getLong("field_id")
            double avg = result.getDouble("average")
            int isStochastic = result.getDouble("minimum") == result.getDouble("maximum") ? 1 : 0
            new PostSimulationCalculation(
                    run: run,
                    keyFigure: PostSimulationCalculation.MEAN,
                    path: PathMapping.get(path),
                    collector: CollectorMapping.get(collector),
                    field: FieldMapping.get(field),
                    period: periodIndex,
                    result: avg).save()
            new PostSimulationCalculation(
                    run: run,
                    keyFigure: PostSimulationCalculation.IS_STOCHASTIC,
                    path: PathMapping.get(path),
                    collector: CollectorMapping.get(collector),
                    field: FieldMapping.get(field),
                    period: periodIndex,
                    result: isStochastic).save()
            if (isStochastic == 0) {
                double[] values = loadValues(path, periodIndex, collector, field)
                if (keyFigures.get(PostSimulationCalculation.STDEV)) {
                    calculateStandardDeviation(periodIndex, path, collector, field, values, avg)
                    completedCalculations++
                }
                def percentiles = keyFigures.get(PostSimulationCalculation.PERCENTILE)
                def vars = keyFigures.get(PostSimulationCalculation.VAR)
                def tvars = keyFigures.get(PostSimulationCalculation.TVAR)
                def pdf = keyFigures.get(PostSimulationCalculation.PDF)
                if (percentiles || vars || tvars || pdf) {
                    percentiles.each {double p ->
                        calculatePercentile(periodIndex, path, collector, field, values, p)
                        completedCalculations++
                    }
                    vars.each {double p ->
                        calculateVar(periodIndex, path, collector, field, values, p, avg)
                        completedCalculations++
                    }
                    tvars.each {double p ->
                        calculateTvar(periodIndex, path, collector, field, values, p)
                        completedCalculations++
                    }
                    if (pdf) {
                        calculatePDF(periodIndex, path, collector, field, values, pdf)
                    }
                }
            }
        }
        LOG.info("Post Simulation Calculation done in ${System.currentTimeMillis() - startTime}ms (#paths ${paths.size()})")


    }

    /**
     *  Values of path and periodIndex are loaded from the database and returned in a sorted double[]
     */
    private double[] loadValues(long pathId, int periodIndex, long collector, long fieldId) {

        long time = System.currentTimeMillis()

        double[] results = ResultAccessor.getValuesSorted(run, periodIndex, pathId, collector, fieldId) as double[]

        LOG.debug("Loaded results for calculations ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
        return results
    }


    private void calculateStandardDeviation(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double mean) {
        long time = System.currentTimeMillis()

        Double stdev = MathUtils.calculateStandardDeviation(results, mean)
        new PostSimulationCalculation(
                run: run,
                keyFigure: PostSimulationCalculation.STDEV,
                path: PathMapping.get(pathId),
                collector: CollectorMapping.get(collectorId),
                field: FieldMapping.get(fieldId),
                period: periodIndex,
                result: stdev).save()

        LOG.debug("Calculated stdev ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculatePercentile(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double percentile) {
        long time = System.currentTimeMillis()

        BigDecimal p = MathUtils.calculatePercentileOfSortedValues(results, percentile)
        new PostSimulationCalculation(
                run: run,
                keyFigure: PostSimulationCalculation.PERCENTILE,
                keyFigureParameter: percentile,
                path: PathMapping.get(pathId),
                collector: CollectorMapping.get(collectorId),
                field: FieldMapping.get(fieldId),
                period: periodIndex,
                result: p).save()

        LOG.debug("Calculated percentile $percentile ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculatePDF(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, def pdf) {
        long time = System.currentTimeMillis()
        HistogramDataset data = new KiloHistogramDataset()
        Map pdfData = data.createPdfData(results, run, pdf)
        pdfData.each {BigDecimal k, v ->
            new PostSimulationCalculation(
                    run: run,
                    keyFigure: PostSimulationCalculation.PDF,
                    keyFigureParameter: k,
                    path: PathMapping.get(pathId),
                    collector: CollectorMapping.get(collectorId),
                    field: FieldMapping.get(fieldId),
                    period: periodIndex,
                    result: v).save()
        }

        LOG.debug("Calculated pdf $pdf ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }


    private void calculateVar(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double percentile, double mean) {
        long time = System.currentTimeMillis()

        BigDecimal var = MathUtils.calculateVarOfSortedValues(results, percentile, mean)
        new PostSimulationCalculation(
                run: run,
                keyFigure: PostSimulationCalculation.VAR,
                keyFigureParameter: percentile,
                path: PathMapping.get(pathId),
                collector: CollectorMapping.get(collectorId),
                field: FieldMapping.get(fieldId),
                period: periodIndex,
                result: var).save()

        LOG.debug("Calculated var $percentile ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

    private void calculateTvar(int periodIndex, long pathId, long collectorId, long fieldId, double[] results, double percentile) {
        long time = System.currentTimeMillis()

        BigDecimal tvar = MathUtils.calculateTvarOfSortedValues(results, percentile)
        new PostSimulationCalculation(
                run: run,
                keyFigure: PostSimulationCalculation.TVAR,
                keyFigureParameter: percentile,
                path: PathMapping.get(pathId),
                collector: CollectorMapping.get(collectorId),
                field: FieldMapping.get(fieldId),
                period: periodIndex,
                result: tvar).save()

        LOG.debug("Calculated tvar $percentile ($pathId, period: $periodIndex) in ${System.currentTimeMillis() - time}ms")
    }

}

