package org.pillarone.riskanalytics.core.simulation

import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter

class ModelStructureWriter implements IConfigObjectWriter {


    void write(ConfigObject configObject, BufferedWriter writer) {
        writer.append(configObject.model.getPackage().toString()).append("\n\n")
        printConfigObject("", configObject, writer, false, -1)
        writer.flush()
    }

    void printConfigObject(prefix, ConfigObject configObject, BufferedWriter out, boolean writePrefix, iteration) {

        boolean prefixRequired = writePrefix //&& configObject.flatten().size() > 0
        if (prefixRequired) {
            writeTabs(iteration, out)
            out << "$prefix {\n"
        }
        ++iteration
        configObject.each {key, value ->
            if (value instanceof ConfigObject) {
                printConfigObject(key, value, out, true, iteration)
            } else {
                def keyString = prefix ? "${prefix}.${key}" : "$key"
                writeLine(key, keyString, out, value, iteration)
            }
        }
        if (prefixRequired) {
            writeTabs(iteration - 1, out)
            out << "}\n"
        }
    }

    private def writeTabs(count, writer) {
        count.times {
            writer << '\t'
        }
    }

    protected def writeLine(key, keyString, BufferedWriter out, value, iteration = 0) {
        writeTabs(iteration, out)
        if (iteration == 0) {
            out << key << "="
            appendValue(out, value)
        } else {
            out << key
        }
        out.newLine()
    }

    private void appendValue(BufferedWriter out, IParameterObject value) {
        out << value.type.getConstructionString(value.parameters)
    }

    private void appendValue(BufferedWriter out, Class value) {
        out << "${value.name}" as Object
    }

    private void appendValue(BufferedWriter out, String value) {
        out << "'$value'"
    }

    private void appendValue(BufferedWriter out, GString value) {
        out << "\"$value\""
    }

    private void appendValue(BufferedWriter out, Object value) {
        out << value.toString()
    }


}

