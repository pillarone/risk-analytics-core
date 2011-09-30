package org.pillarone.riskanalytics.core.report

import net.sf.jasperreports.engine.JRDataSource
import org.pillarone.riskanalytics.core.simulation.item.Simulation


public interface IReportModel {

    String getName()

    URL getMainReportFile()

    List<URL> getAllSourceFiles()

    JRDataSource getDataSource(IReportData reportData)

    Map getParameters(IReportData reportData)

    String getDefaultReportFileNameWithoutExtension(IReportData reportData)
}