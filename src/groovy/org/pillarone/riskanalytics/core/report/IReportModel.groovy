package org.pillarone.riskanalytics.core.report

import net.sf.jasperreports.engine.JRDataSource
import org.pillarone.riskanalytics.core.report.ReportFactory
import org.pillarone.riskanalytics.core.report.ReportFactory.ReportFormat

public interface IReportModel {

    String getName()

    URL getMainReportFile()

    JRDataSource getDataSource(IReportData reportData)

    Map getParameters(IReportData reportData)

    String getDefaultReportFileNameWithoutExtension(IReportData reportData)

    boolean isValidFormatAndData(ReportFactory.ReportFormat reportFormat, IReportData reportData)
}