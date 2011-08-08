package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.util.IConfigObjectWriter

class ResultConfigurationWriter implements IConfigObjectWriter {


    void write(ConfigObject configObject, BufferedWriter writer) {
        writer.append(configObject.model.getPackage().toString()).append("\n\n")
        printConfigObject("", configObject, writer, false, -1)
        writer.flush()
    }

    void printConfigObject(prefix, ConfigObject configObject, BufferedWriter out, boolean writePrefix, iteration) {

        boolean prefixRequired = writePrefix && configObject.flatten().size() > 0
        if (prefixRequired) {
            writeTabs(iteration, out)
            out << "$prefix {\n"
        }
        ++iteration
        configObject.each {key, value ->
            def keyString = prefix ? "${prefix}.${key}" : "$key"
            if (value instanceof ConfigObject) {
                printConfigObject(key, value, out, true, iteration)
            }
            else {
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

        if (keyString.contains('.')) {
            keyString = "${keyString.substring(0, keyString.lastIndexOf('.'))}[$key]"
        }
        out << key << " = "
        appendValue(out, value)

        out.newLine()
    }

    private void appendValue(BufferedWriter out, Enum value) {
        out << "${value.getDeclaringClass().name}.$value"
    }

    private void appendValue(BufferedWriter out, Class value) {
        out << "${value.name}"
    }

    private void appendValue(BufferedWriter out, def value) {
    }

    private void appendValue(BufferedWriter out, String value) {
        out << "\"" + value + "\""
    }

    private void appendValue(BufferedWriter out, GString value) {
        out << "\"" + value + "\""
    }

}