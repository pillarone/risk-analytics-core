package org.pillarone.riskanalytics.core.report

import org.pillarone.riskanalytics.core.simulation.item.Simulation
import net.sf.jasperreports.engine.JRExporterParameter
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.util.JRLoader
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JRExporter
import net.sf.jasperreports.engine.export.JRPdfExporter


abstract class ReportFactory {

    public static byte[] createPDFReport(IReportModel reportModel, Simulation simulation) {
        JRExporter exporter = new JRPdfExporter()

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream)

        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportModel.reportFile);
        jasperReport.setWhenNoDataType(jasperReport.WHEN_NO_DATA_TYPE_ALL_SECTIONS_NO_DETAIL);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportModel.getParameters(simulation), reportModel.getDataSource(simulation))

        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint)
        exporter.exportReport()

        return byteArrayOutputStream.toByteArray()
    }

}
