package models.core

import org.pillarone.riskanalytics.core.simulation.engine.BatchRunTest


class CoreBatchRunTests extends BatchRunTest {

    @Override
    Class getModelClass() {
        CoreModel
    }

}
