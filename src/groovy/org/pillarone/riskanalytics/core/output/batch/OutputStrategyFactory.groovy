package org.pillarone.riskanalytics.core.output.batch

import org.pillarone.riskanalytics.core.output.DBOutput
import org.pillarone.riskanalytics.core.output.FileOutput
import org.pillarone.riskanalytics.core.output.NoOutput
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.OutputStrategy

class OutputStrategyFactory {
    static List OUTPUT_TYPES = [DBOutput.class, FileOutput.class, NoOutput.class]
    static List ENUMS = [org.pillarone.riskanalytics.core.output.OutputStrategy.BATCH_DB_OUTPUT, org.pillarone.riskanalytics.core.output.OutputStrategy.FILE_OUTPUT, org.pillarone.riskanalytics.core.output.OutputStrategy.NO_OUTPUT]

    public static ICollectorOutputStrategy getInstance(OutputStrategy strategy) {
        switch (strategy) {
            case org.pillarone.riskanalytics.core.output.OutputStrategy.BATCH_DB_OUTPUT: return new DBOutput()
            case org.pillarone.riskanalytics.core.output.OutputStrategy.FILE_OUTPUT: return new FileOutput()
            case org.pillarone.riskanalytics.core.output.OutputStrategy.NO_OUTPUT: return new NoOutput()
        }

    }

    public static OutputStrategy getEnum(Class clazz) {
        return ENUMS[OUTPUT_TYPES.indexOf(clazz)]
    }

}