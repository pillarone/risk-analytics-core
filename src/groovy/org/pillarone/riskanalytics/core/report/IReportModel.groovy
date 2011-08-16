package org.pillarone.riskanalytics.core.report

import net.sf.jasperreports.engine.JRDataSource
import org.pillarone.riskanalytics.core.simulation.item.Simulation


public interface IReportModel {

    String getName()

    URL getReportFile()

    JRDataSource getDataSource(Simulation simulation)

    Map getParameters(Simulation simulation)

}