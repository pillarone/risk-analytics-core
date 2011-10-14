package org.pillarone.riskanalytics.core.report

import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter
import net.sf.jasperreports.engine.util.JRLoader
import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.export.JRXlsExporter
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

abstract class ReportFactory {

    private static Log LOG = LogFactory.getLog(ReportFactory)

    public enum ReportFormat {

        PDF (new JRPdfExporter(), "PDF", "pdf"),
        PPT  (new JRPptxExporter(), "PowerPoint", "pptx"),
        XLS  (new JRXlsExporter(), "Excel", "xls")

        private final JRExporter jrExporter
        private final String renderedFormatSuchAsPDF
        private final String fileExtension

        ReportFormat(JRExporter jrExporter, String renderedFormatSuchAsPDF, String fileExtension) {
            this.jrExporter = jrExporter
            this.renderedFormatSuchAsPDF = renderedFormatSuchAsPDF
            this.fileExtension = fileExtension
        }

        public JRExporter getExporter() {
            jrExporter
        }

        public String getRenderedFormatSuchAsPDF() {
            renderedFormatSuchAsPDF
        }

        public String getFileExtension() {
            fileExtension
        }
    }

    public static byte[] createPDFReport(IReportModel reportModel, IReportData reportData) {
        return createReport(reportModel, reportData, new JRPdfExporter())
    }

    public static byte[] createPPTXReport(IReportModel reportModel, IReportData reportData) {
        return createReport(reportModel, reportData, new JRPptxExporter())
    }

    public static byte[] createXLSReport(IReportModel reportModel, IReportData reportData) {
        return createReport(reportModel, reportData, new JRXlsExporter())
    }

    public static byte[] createReport(IReportModel reportModel, IReportData reportData, ReportFormat format) {
        return createReport(reportModel, reportData, format.getExporter())
    }

    public static byte[] createReport(IReportModel reportModel, IReportData reportData, JRExporter exporter) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream)

        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportModel.mainReportFile);
        jasperReport.setWhenNoDataType(jasperReport.WHEN_NO_DATA_TYPE_ALL_SECTIONS_NO_DETAIL);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportModel.getParameters(reportData), reportModel.getDataSource(reportData))

        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint)
        exporter.exportReport()

        return byteArrayOutputStream.toByteArray()

    }

}
