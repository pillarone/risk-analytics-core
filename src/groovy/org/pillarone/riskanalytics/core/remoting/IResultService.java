package org.pillarone.riskanalytics.core.remoting;

import java.util.List;

public interface IResultService {

    List<ParameterizationInfo> getParameterizationInfosForTransactionId(long dealId);

    List<SimulationInfo> getSimulationInfos(long parameterizationId);

    List<ResultInfo> getResults(long simulationId, List<String> paths);
}
