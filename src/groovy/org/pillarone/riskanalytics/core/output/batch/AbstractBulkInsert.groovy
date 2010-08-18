package org.pillarone.riskanalytics.core.output.batch

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.FileConstants
import org.pillarone.riskanalytics.core.output.SimulationRun

abstract class AbstractBulkInsert {
    
    protected static final Log LOG = LogFactory.getLog(AbstractBulkInsert)

    File tempFile
    BufferedWriter writer
    SimulationRun simulationRun
    //cache simulation run id, to avoid using domain objects too often (causes unnecessary hibernate calls in the background)
    long simulationRunId
    boolean initialized = false

    private void init() {
        if (!initialized) {
            String filename = "${FileConstants.TEMP_FILE_DIRECTORY}${File.separatorChar}${simulationRunId}"
            LOG.info("Temp file at: $filename")

            tempFile = new File(filename)
            tempFile.delete()
            writer = tempFile.newWriter(true)
            initialized = true
        }
    }

    void setSimulationRun(SimulationRun simulationRun) {
        this.@simulationRun = simulationRun
        this.simulationRunId = simulationRun.id
        init()
    }

    void setSimulationRunId(long id){
        this.simulationRunId=id;
        init();
    }

    abstract protected void writeResult(List values)

    final void saveToDB() {
        if (initialized) {
            writer.flush()
            writer.close()
            save()
            tempFile.delete()
        }
    }

    void reset() {
        if (initialized) {
            writer.close()
            tempFile.delete()
            initialized = false
            simulationRun = null
        }
    }

    abstract protected void save()
}
