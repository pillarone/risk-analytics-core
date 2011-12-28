package org.pillarone.riskanalytics.core.report

import org.pillarone.riskanalytics.core.report.impl.ReportDataCollection
import org.pillarone.riskanalytics.core.report.impl.ModellingItemReportData
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.Parameterization

/**
 * bzetterstrom
 */
abstract class ReportUtils {
    public static ModellingItem getSingleModellingItem(IReportData reportData) {
        ModellingItem modellingItem = null;
        reportData.accept(new IReportDataVisitor() {
            void visitModellingItemReportData(ModellingItemReportData modellingItemReportData) {
                modellingItem = (ModellingItem)modellingItemReportData.item
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
        modellingItem
    }

    public static boolean isSingleItem(final IReportData reportData, final Class expectedItemType) {
        boolean isSingleItem = false;
        reportData.accept(new IReportDataVisitor() {
            void visitModellingItemReportData(ModellingItemReportData modellingItemReportData) {
                isSingleItem = expectedItemType.isAssignableFrom(modellingItemReportData.item.getClass())
            }

            void visitReportDataCollection(ReportDataCollection reportDataCollection) {
                if (reportDataCollection.size() != 1) {
                    isSingleItem = false
                } else {
                    reportDataCollection.get(0).accept(this)
                }
            }
        })
        isSingleItem
    }

    public static Collection<ModellingItem> getModellingItemCollection(IReportData reportData) {
        List<ModellingItem> modellingItems = new ArrayList<ModellingItem>();
        reportData.accept(new IReportDataVisitor() {
            void visitModellingItemReportData(ModellingItemReportData modellingItemReportData) {
                modellingItems.add((ModellingItem)modellingItemReportData.item)
            }

            void visitReportDataCollection(ReportDataCollection reportDataCollection) {
                for (IReportData reportData2: reportDataCollection) {
                    reportData2.accept(this)
                }
            }
        })
        modellingItems
    }

    public static void loadAndApplyParameterizationToModel(Model model, Parameterization parameterization) {
        model.init()

        ParameterApplicator applicator = new ParameterApplicator()
        applicator.model = model
        parameterization.load(true)
        applicator.parameterization = parameterization
        applicator.init()
        applicator.applyParameterForPeriod(0)
    }

}
