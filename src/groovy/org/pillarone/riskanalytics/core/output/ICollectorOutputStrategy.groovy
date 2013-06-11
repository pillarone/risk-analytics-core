package org.pillarone.riskanalytics.core.output


public interface ICollectorOutputStrategy {


    ICollectorOutputStrategy leftShift(List<SingleValueResultPOJO> results)

    void finish()

}
