package org.pillarone.riskanalytics.core.cli

import org.apache.commons.cli.CommandLine
import org.pillarone.riskanalytics.core.output.FileOutput
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.DBOutput
import org.pillarone.riskanalytics.core.output.NoOutput


class ArgumentParserTests extends GroovyTestCase {

    void testValidArguments() {
        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        assertNotNull argumentParser.parseArguments(args)
    }

    void testOptionGroup() {
        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        assertNotNull argumentParser.parseArguments(args)

        argumentParser = new ArgumentParser()
        args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.NO_OUTPUT_OPTION
        ] as String[]
        assertNotNull argumentParser.parseArguments(args)

        argumentParser = new ArgumentParser()
        args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.DB_OUTPUT_OPTION
        ] as String[]
        assertNotNull argumentParser.parseArguments(args)

        argumentParser = new ArgumentParser()
        args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
                "-" + ArgumentParser.DB_OUTPUT_OPTION
        ] as String[]
        assertNull argumentParser.parseArguments(args)
    }

    void testUnknownArguments() {
        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-periods", "3",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        assertNull argumentParser.parseArguments(args)
    }

    void testMissingArguments() {
        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        assertNull argumentParser.parseArguments(args)

        argumentParser = new ArgumentParser()
        args = [
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."

        ] as String[]
        assertNull argumentParser.parseArguments(args)

        argumentParser = new ArgumentParser()
        args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        assertNull argumentParser.parseArguments(args)
    }

    void testOutputStrategy() {
        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine
        ICollectorOutputStrategy outputStrategy = argumentParser.getOutputStrategy(commandLine)
        assertEquals FileOutput.class.name, outputStrategy.class.name
        assertEquals ".", ((FileOutput) outputStrategy).resultLocation

        argumentParser = new ArgumentParser()
        args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.DB_OUTPUT_OPTION
        ] as String[]
        commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine
        outputStrategy = argumentParser.getOutputStrategy(commandLine)
        assertEquals DBOutput.class.name, outputStrategy.class.name

        argumentParser = new ArgumentParser()
        args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "path/to/param",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "path/to/param",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.NO_OUTPUT_OPTION
        ] as String[]
        commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine
        outputStrategy = argumentParser.getOutputStrategy(commandLine)
        assertEquals NoOutput.class.name, outputStrategy.class.name
    }
}
