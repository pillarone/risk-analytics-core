package org.pillarone.riskanalytics.core.report;

import org.pillarone.riskanalytics.core.report.impl.ReportDataCollection;
import org.pillarone.riskanalytics.core.report.impl.ModellingItemReportData;
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem;
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator;
import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * bzetterstrom
 */
public abstract class ReportUtils {
    public static ModellingItem getSingleModellingItem(IReportData reportData) {
        final ModellingItem[] modellingItem = new ModellingItem[1];
        reportData.accept(new IReportDataVisitor() {
            public void visitModellingItemReportData(ModellingItemReportData modellingItemReportData) {
                modellingItem[0] = modellingItemReportData.getItem();
            }

            public void visitReportDataCollection(ReportDataCollection reportDataCollection) {
                if (reportDataCollection.size() == 0) {
                    throw new UnsupportedReportParameterException("Please select an item for this report");
                } else if (reportDataCollection.size() > 1) {
                    throw new UnsupportedReportParameterException("Please select only one item for this report");
                } else {
                    reportDataCollection.get(0).accept(this);
                }
            }
        });
        return modellingItem[0]; //TODO: ugly
    }

    public static boolean isSingleItem(final IReportData reportData, final Class expectedItemType) {
        final boolean[] isSingleItem = new boolean[1];
        reportData.accept(new IReportDataVisitor() {
            public void visitModellingItemReportData(ModellingItemReportData modellingItemReportData) {
                isSingleItem[0] = expectedItemType.isAssignableFrom(modellingItemReportData.getItem().getClass());
            }

            public void visitReportDataCollection(ReportDataCollection reportDataCollection) {
                if (reportDataCollection.size() != 1) {
                    isSingleItem[0] = false;
                } else {
                    reportDataCollection.get(0).accept(this);
                }
            }
        });
        return isSingleItem[0]; //TODO:
    }

    public static Collection<ModellingItem> getModellingItemCollection(IReportData reportData) {
        final List<ModellingItem> modellingItems = new ArrayList<ModellingItem>();
        reportData.accept(new IReportDataVisitor() {
            public void visitModellingItemReportData(ModellingItemReportData modellingItemReportData) {
                modellingItems.add(modellingItemReportData.getItem());
            }

            public void visitReportDataCollection(ReportDataCollection reportDataCollection) {
                for (IReportData reportData2 : reportDataCollection) {
                    reportData2.accept(this);
                }
            }
        });
        return modellingItems;
    }

    public static void loadAndApplyParameterizationToModel(Model model, Parameterization parameterization) throws ParameterApplicator.ApplicableParameterCreationException {
        model.init();

        ParameterApplicator applicator = new ParameterApplicator();
        applicator.setModel(model);
        parameterization.load(true);
        applicator.setParameterization(parameterization);
        applicator.init();
        applicator.applyParameterForPeriod(0);
    }

}
