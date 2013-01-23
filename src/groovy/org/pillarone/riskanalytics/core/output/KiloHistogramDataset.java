package org.pillarone.riskanalytics.core.output;

import org.jfree.data.statistics.HistogramBin;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: fja
 * Date: 16-Dec-2009
 * Time: 15:37:31
 * To change this template use File | Settings | File Templates.
 */
class KiloHistogramDataset extends HistogramDataset {

    public Number getY(int series, int item) {
        Double y = (Double) super.getY(series, item);
        return y * 1000;
    }

    public Number getStartY(int series, int item) {
        Double y = (Double) super.getStartY(series, item);
        return y * 1000;
    }

    public Map createPdfData(double[] results, SimulationRun run, int pdf) {
        Map seriesPDF = new HashMap();
        setType(HistogramType.SCALE_AREA_TO_1);
        double upperBound = (Double) results[results.length - 1];
        double lowerBound = (Double) results[0];
        int pdfSize = (int) Math.min(results.length, pdf);

        addSeries("legendTitle", results, pdfSize, lowerBound, upperBound);
        double normalization = binWidth(0) * run.getIterations();

        HistogramBin bin = (HistogramBin) bins(0).get(0);
        double x = bin.getStartBoundary();
        seriesPDF.put(x, 0d);
        x += binWidth(0) / 2.0;

        double y = 0d;
        for (int i = 0; i < pdfSize; i += 1) {
            HistogramBin bin2 = (HistogramBin) bins(0).get(i);
            y = bin2.getCount() / normalization;
            seriesPDF.put(x, y * 1000000.0);
            x += binWidth(0);
        }
        HistogramBin bin2 = (HistogramBin) bins(0).get(pdfSize - 1);
        seriesPDF.put(bin2.getEndBoundary(), 0d);
        return seriesPDF;
    }

    private double binWidth(int series) {
        try {
            Method getBinWidth = HistogramDataset.class.getDeclaredMethod("getBinWidth", Integer.TYPE);
            getBinWidth.setAccessible(true);
            return (Double) getBinWidth.invoke(this, new Object[]{series});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List bins(int series) {
        try {
            Method getBins = HistogramDataset.class.getDeclaredMethod("getBins", Integer.TYPE);
            getBins.setAccessible(true);
            return (List) getBins.invoke(this, new Object[]{series});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
