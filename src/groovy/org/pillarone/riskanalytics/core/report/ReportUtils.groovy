package org.pillarone.riskanalytics.core.report

import org.pillarone.riskanalytics.core.report.impl.ReportDataCollection
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.report.impl.ModellingItemReportData

/**
 * bzetterstrom
 */
abstract class ReportUtils {
    public static Simulation getSingleSimulation(IReportData reportData) {
        Simulation simulation = null;
        reportData.accept(new IReportDataVisitor() {
            void visitModellingItemReportData(ModellingItemReportData modellingItemReportData) {
                simulation = (Simulation)modellingItemReportData.item
            }

            void visitReportDataCollection(ReportDataCollection reportDataCollection) {
                if (reportDataCollection.size() == 0) {
                    throw new UnsupportedReportParameterException("Please select an item for this report")
                } else if (reportDataCollection.size() > 1) {
                    throw new UnsupportedReportParameterException("Please select only one item for this report")
                } else {
                    reportDataCollection.get(0).accept(this)
                }
            }
        })
        simulation
    }
}
