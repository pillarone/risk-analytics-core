package org.pillarone.riskanalytics.core.output.batch

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.output.DBOutput
import org.pillarone.riskanalytics.core.output.FileOutput
import org.pillarone.riskanalytics.core.output.NoOutput
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.OutputStrategy

@CompileStatic
class OutputStrategyFactory {
    static List<Class> OUTPUT_TYPES = [DBOutput.class, FileOutput.class, NoOutput.class]
    static List<OutputStrategy> ENUMS = [OutputStrategy.BATCH_DB_OUTPUT, OutputStrategy.FILE_OUTPUT, OutputStrategy.NO_OUTPUT]

    public static ICollectorOutputStrategy getInstance(OutputStrategy strategy) {
        switch (strategy) {
            case OutputStrategy.BATCH_DB_OUTPUT: return new DBOutput()
            case OutputStrategy.FILE_OUTPUT: return new FileOutput()
            case OutputStrategy.NO_OUTPUT: return new NoOutput()
        }

    }

    public static OutputStrategy getEnum(Class clazz) {
        return ENUMS[OUTPUT_TYPES.indexOf(clazz)]
    }

}