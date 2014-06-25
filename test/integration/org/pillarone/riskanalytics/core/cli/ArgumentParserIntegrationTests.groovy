package org.pillarone.riskanalytics.core.cli

import models.core.CoreModel
import org.apache.commons.cli.CommandLine
import org.junit.Test
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.fileimport.ResultConfigurationImportService
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder

import static org.junit.Assert.*

class ArgumentParserIntegrationTests {

    @Test
    void testOptionalArguments() {
        new ParameterizationImportService().compareFilesAndWriteToDB(['Core'])

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
                "-" + ArgumentParser.SEED_OPTION, "123",
                "-" + ArgumentParser.SIMULATION_NAME_OPTION, "name",
                "-" + ArgumentParser.COMMENT_OPTION, "my comment",
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals 123, simulation.randomSeed
        assertEquals "name", simulation.name
        assertEquals "my comment", simulation.comment

    }

    @Test
    void testForceParameterizationWithExisting() {
        new ParameterizationImportService().compareFilesAndWriteToDB(['Core'])

        int initialParameterizationCount = ParameterizationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
                "-" + ArgumentParser.FORCE_OPTION
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation
        //must never be null even if the option isn't given
        assertNotNull simulation.randomSeed
        assertNotNull simulation.modelVersionNumber

        assertEquals initialParameterizationCount + 1, ParameterizationDAO.count()
        assertEquals "2", simulation.parameterization.versionNumber.toString()
    }

    @Test
    void testForceResultConfigurationWithExisting() {
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['Core'])

        int initialResultConfigurationCount = ResultConfigurationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
                "-" + ArgumentParser.FORCE_OPTION
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialResultConfigurationCount + 1, ResultConfigurationDAO.count()
        assertEquals "2", simulation.template.versionNumber.toString()
    }

    @Test
    void testForceParameterizationWithoutExisting() {

        int initialParameterizationCount = ParameterizationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
                "-" + ArgumentParser.FORCE_OPTION
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialParameterizationCount + 1, ParameterizationDAO.count()
        assertEquals "1", simulation.parameterization.versionNumber.toString()
    }

    @Test
    void testForceResultConfigurationWithoutExisting() {

        int initialResultConfigurationCount = ResultConfigurationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
                "-" + ArgumentParser.FORCE_OPTION
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialResultConfigurationCount + 1, ResultConfigurationDAO.count()
        assertEquals "1", simulation.template.versionNumber.toString()
    }

    @Test
    void testParameterizationWithoutExisting() {

        int initialParameterizationCount = ParameterizationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialParameterizationCount + 1, ParameterizationDAO.count()
        assertEquals "1", simulation.parameterization.versionNumber.toString()
    }

    @Test
    void testResultConfigurationWithoutExisting() {

        int initialResultConfigurationCount = ResultConfigurationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialResultConfigurationCount + 1, ResultConfigurationDAO.count()
        assertEquals "1", simulation.template.versionNumber.toString()
    }

    @Test
    void testParameterizationWithSameExisting() {
        new ParameterizationImportService().compareFilesAndWriteToDB(['Core'])

        int initialParameterizationCount = ParameterizationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialParameterizationCount, ParameterizationDAO.count()
        assertEquals "1", simulation.parameterization.versionNumber.toString()
    }

    @Test
    void testResultConfigurationWithSameExisting() {
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['Core'])

        int initialResultConfigurationCount = ResultConfigurationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialResultConfigurationCount, ResultConfigurationDAO.count()
        assertEquals "1", simulation.template.versionNumber.toString()
    }

    @Test
    void testParameterizationWithDifferentExisting() {
        new ParameterizationImportService().compareFilesAndWriteToDB(['Core'])

        Parameterization parameterizationToChange = new Parameterization("CoreParameters")
        parameterizationToChange.modelClass = CoreModel
        parameterizationToChange.load()

        ParameterObjectParameterHolder holder = parameterizationToChange.allParameterHolders.find {it.path=='exampleInputOutputComponent:parmNewParameterObject'}
        holder.setValue("TYPE1")
        parameterizationToChange.save()

        int initialParameterizationCount = ParameterizationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, "."
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialParameterizationCount + 1, ParameterizationDAO.count()
        assertEquals "2", simulation.parameterization.versionNumber.toString()
    }

    @Test
    void testResultConfigurationWithDifferentExisting() {
        new ResultConfigurationImportService().compareFilesAndWriteToDB(['Core'])

        ResultConfiguration resultConfigurationToChange = new ResultConfiguration("CoreResultConfiguration", CoreModel)
        resultConfigurationToChange.load()
        PacketCollector collector = resultConfigurationToChange.collectors[0]
        if(collector.mode.class == SingleValueCollectingModeStrategy) {
            collector.mode = new AggregatedCollectingModeStrategy()
        } else {
            collector.mode = new SingleValueCollectingModeStrategy()
        }
        resultConfigurationToChange.save()

        int initialResultConfigurationCount = ResultConfigurationDAO.count()

        ArgumentParser argumentParser = new ArgumentParser()
        String[] args = [
                "-" + ArgumentParser.PARAMETERIZATION_OPTION, "src/java/models/core/CoreParameters.groovy",
                "-" + ArgumentParser.RESULT_CONFIGURATION_OPTION, "src/java/models/core/CoreResultConfiguration.groovy",
                "-" + ArgumentParser.ITERATIONS_OPTION, "1000",
                "-" + ArgumentParser.FILE_OUTPUT_OPTION, ".",
        ] as String[]
        CommandLine commandLine = argumentParser.parseArguments(args)
        assertNotNull commandLine

        Simulation simulation = argumentParser.createSimulation(commandLine)
        assertNotNull simulation

        assertEquals initialResultConfigurationCount + 1, ResultConfigurationDAO.count()
        assertEquals "2", simulation.template.versionNumber.toString()
    }


}
