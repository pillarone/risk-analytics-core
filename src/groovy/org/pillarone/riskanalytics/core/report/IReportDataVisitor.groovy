package org.pillarone.riskanalytics.core.report

import org.pillarone.riskanalytics.core.report.impl.ModellingItemReportData
import org.pillarone.riskanalytics.core.report.impl.ReportDataCollection

/**
 * Created by IntelliJ IDEA.
 * User: bzetterstrom
 * Date: 26/09/11
 * Time: 09:55
 * To change this template use File | Settings | File Templates.
 */
public interface IReportDataVisitor {
    void visitModellingItemReportData(ModellingItemReportData modellingItemReportData);
    void visitReportDataCollection(ReportDataCollection reportDataCollection);
}
