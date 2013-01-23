package org.pillarone.riskanalytics.core.output

/**
 * Created by IntelliJ IDEA.
 * User: fja
 * Date: 16-Dec-2009
 * Time: 15:37:31
 * To change this template use File | Settings | File Templates.
 *
 * Calculates the probability density function from sample by using a kernel density estimator
 * with characteristic functions.
 *
 */
class PdfFromSample {

    public Map createPdfData(double[] results, int pdf) {
        Map seriesPDF = [:]
        double upperBound = (Double) results[results.length - 1]
        double lowerBound = (Double) results[0]
        int pdfSize = (int) Math.min(results.length, pdf)

        double binWidth = (upperBound - lowerBound) / (double) (pdfSize - 1.0)
        double normalizationConstant = binWidth * results.length

        double x = results[0] - 1 / 2d * binWidth
        seriesPDF.put(x, 0d)
        x += binWidth / 2d

        int j = 0
        for (int i = 0; i < pdfSize; i += 1) {
            int count = 0;
            while (j < results.length && results[j] < x + 1 / 2d * binWidth) {
                count++
                j++
            }
            seriesPDF.put(x, (double) count / normalizationConstant)
            x += binWidth
        }
        seriesPDF.put(x - binWidth / 2d, 0d)
        return seriesPDF
    }
}
