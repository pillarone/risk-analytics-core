package org.pillarone.riskanalytics.core.simulation.engine.grid.output


import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.FileConstants

class ResultWriter {

    private String simulationRunPath;
    private static Log LOG = LogFactory.getLog(ResultWriter)

    public ResultWriter(long simulationRunId) {

        simulationRunPath = "${FileConstants.EXTERNAL_DATABASE_DIRECTORY}${File.separator}simulations${File.separator}${simulationRunId}"
        new File(simulationRunPath).mkdirs();
    }


    public void writeResult(ResultTransferObject intermediateResult) {
        String fileName = intermediateResult.getResultDescriptor().getFileName()
        byte[] content = intermediateResult.getData()

        File tempFile = new File(simulationRunPath + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(tempFile, true);
        fos.write(content);
        fos.close();
    }

}
