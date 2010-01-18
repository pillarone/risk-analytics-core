package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.output.SingleValueResult
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy

public class FileOutput implements ICollectorOutputStrategy {

    String resultLocation

    public ICollectorOutputStrategy leftShift(List results) {

        File resultFile = new File(getFileName(results[0]))
        if (!resultFile.exists()) {
            resultFile.withWriter {writer ->
                writer.writeLine("iteration\tperiod\tpath\tfield\tvalue")
            }
        }

        resultFile.withWriterAppend {writer ->
            for (SingleValueResult result in results) {
                writer.writeLine("${result.iteration}\t${result.period}\t${result.path.pathName}\t${result.field.fieldName}\t${result.value}")
            }
        }

        return this
    }

    private String getFileName(SingleValueResult result){
        return "${resultLocation}${File.separator}${replaceChars(result.simulationRun.name)}.tsl"
    }

    private String replaceChars(String s) {
        char replacement = "_" as char
        
        s = s.replace(":" as char, replacement)
        s = s.replace("\\" as char, replacement)
        s = s.replace("/" as char, replacement)
        s = s.replace("*" as char, replacement)
        s = s.replace("?" as char, replacement)
        s = s.replace("\"" as char, replacement)
        s = s.replace("<" as char, replacement)
        s = s.replace(">" as char, replacement)
        s = s.replace("|" as char, replacement)

        return s
    }

    public void finish() {

    }


}
