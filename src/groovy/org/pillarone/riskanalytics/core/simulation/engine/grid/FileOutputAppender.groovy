package org.pillarone.riskanalytics.core.simulation.engine.grid


import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.FileConstants

class FileOutputAppender {

    private String simulationRunPath;
    private static Log LOG = LogFactory.getLog(FileOutputAppender)
    
    //
    public void init(long simRunId) {

        simulationRunPath = "${FileConstants.EXTERNAL_DATABASE_DIRECTORY}" + (File.separator + "simrun" + simRunId);
        new File(simulationRunPath).mkdir();
    }


    public synchronized void writeResult(HashMap<String, byte[]> intermediateResult) {
        String fname;
        byte[] content;
        for (String s: intermediateResult.keySet()) {
            fname = s;
            content = intermediateResult.get(fname);
        }
        File tempFile = new File(simulationRunPath + File.separator + fname);
        FileOutputStream fos = new FileOutputStream(tempFile, true);
        fos.write(content);
        fos.close();
    }

}
