package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.BatchRun

class Batch extends ModellingItem {

    Batch(String name) {
        super(name)
    }

    @Override
    protected createDao() {
        new BatchRun(name: name)
    }

    @Override
    def getDaoClass() {
        BatchRun
    }

    @Override
    protected void mapToDao(def Object dao) {

    }

    @Override
    protected void mapFromDao(def Object dao, boolean completeLoad) {

    }

    @Override
    protected loadFromDB() {
        return BatchRun.findByName(name)
    }

    @Override
    Class getModelClass() {
        null
    }
}
