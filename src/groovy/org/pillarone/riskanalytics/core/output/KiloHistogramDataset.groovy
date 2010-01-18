package org.pillarone.riskanalytics.core.output

import org.jfree.data.statistics.HistogramDataset
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.jfree.data.statistics.HistogramType

/**
 * Created by IntelliJ IDEA.
 * User: fja
 * Date: 16-Dec-2009
 * Time: 15:37:31
 * To change this template use File | Settings | File Templates.
 */
class KiloHistogramDataset extends HistogramDataset {

    public Number getY(int series, int item) {
        super.getY(series, item) * 1000
    }

    public Number getStartY(int series, int item) {
        super.getStartY(series, item) * 1000
    }

    public Map createPdfData(double [] results, SimulationRun run, int pdf){
        Map seriesPDF = [:]
        setType HistogramType.SCALE_AREA_TO_1
        double upperBound = (Double) results[results.length - 1]
        double lowerBound = (Double) results[0]
        int pdfSize =(int) Math.min(results.length, pdf)

        addSeries("legendTitle",results, pdfSize, lowerBound, upperBound)
        double normalization= getBinWidth(0) * run.iterations

        BigDecimal x = getBins(0)[0].startBoundary
        seriesPDF.put(x,0d)
        x +=  getBinWidth(0)/2.0

        BigDecimal y = 0d
        for (int i = 0; i < pdfSize; i += 1) {
            y = getBins(0)[i].count/normalization
            seriesPDF.put(x, y*1000000.0 )
            x += getBinWidth(0)
        }
        seriesPDF.put(getBins(0)[pdfSize - 1].endBoundary, 0d)
        return seriesPDF
    }
}
