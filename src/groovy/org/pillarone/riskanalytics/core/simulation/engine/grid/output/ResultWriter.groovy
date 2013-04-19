package org.pillarone.riskanalytics.core.simulation.engine.grid.output

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper

@CompileStatic
class ResultWriter {

    private String simulationRunPath;
    private static Log LOG = LogFactory.getLog(ResultWriter)

    //TODO: check 'too many open files' problems
    private Map<String, FileOutputStream> streamCache = new HashMap<String, FileOutputStream>()

    public ResultWriter(long simulationRunId) {

        simulationRunPath = GridHelper.getResultLocation(simulationRunId)
        File file = new File(simulationRunPath)
        if (file.exists()) {
            file.deleteDir()
        }
        file.mkdirs();
    }


    void writeResult(ResultTransferObject intermediateResult) {
        String fileName = intermediateResult.getResultDescriptor().getFileName()
        byte[] content = intermediateResult.getData()

        FileOutputStream stream = streamCache.get(fileName)
        if (stream == null) {
            File tempFile = new File(simulationRunPath + File.separator + fileName);
            stream = new FileOutputStream(tempFile, true);
            streamCache.put(fileName, stream)
        }

        stream.write(content);
    }

    void close() {
        for(FileOutputStream stream in streamCache.values()) {
            stream.close()
        }
    }

}
