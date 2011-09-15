package org.pillarone.riskanalytics.core.report

import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter
import net.sf.jasperreports.engine.util.JRLoader
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import net.sf.jasperreports.engine.*

abstract class ReportFactory {


    public static byte[] createPDFReport(IReportModel reportModel, Simulation simulation) {
        return createReport(reportModel, simulation, new JRPdfExporter())
    }

    public static byte[] createPPTXReport(IReportModel reportModel, Simulation simulation) {
        return createReport(reportModel, simulation, new JRPptxExporter())
    }

    public static byte[] createReport(IReportModel reportModel, Simulation simulation, JRExporter exporter) {
        compileReport(reportModel)

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream)

        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportModel.mainReportFile);
        jasperReport.setWhenNoDataType(jasperReport.WHEN_NO_DATA_TYPE_ALL_SECTIONS_NO_DETAIL);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportModel.getParameters(simulation), reportModel.getDataSource(simulation))

        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint)
        exporter.exportReport()

        return byteArrayOutputStream.toByteArray()

    }

    private static void compileReport(IReportModel model) {
        for (URL url in model.allSourceFiles) {
            URL jasperURL = new URL(url.toExternalForm().replace("jrxml", "jasper"))
            File jasperFile = new File(jasperURL.toURI())
            if (!jasperFile.exists()) {
                File sourceFile = new File(url.toURI())
                JasperCompileManager.compileReportToStream(sourceFile.newInputStream(), jasperFile.newOutputStream())
            }
        }
    }

}
