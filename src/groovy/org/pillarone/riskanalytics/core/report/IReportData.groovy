package org.pillarone.riskanalytics.core.report

/**
 * This is the base interface for data used by the reports.
 *
 * User: bzetterstrom
 */
public interface IReportData {
    void accept(IReportDataVisitor visitor);
}