package org.pillarone.riskanalytics.core.cli;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.pillarone.riskanalytics.core.output.DBOutput;
import org.pillarone.riskanalytics.core.output.FileOutput;
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy;
import org.pillarone.riskanalytics.core.output.NoOutput;
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper;
import org.pillarone.riskanalytics.core.simulation.item.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

public class ArgumentParser {

    private static Log LOG = LogFactory.getLog(ArgumentParser.class);

    public static final String PARAMETERIZATION_OPTION = "parameterization";
    public static final String RESULT_CONFIGURATION_OPTION = "resultConfiguration";
    public static final String ITERATIONS_OPTION = "iterations";

    public static final String NO_OUTPUT_OPTION = "nooutput";
    public static final String FILE_OUTPUT_OPTION = "fileoutput";
    public static final String DB_OUTPUT_OPTION = "dboutput";

    public static final String FORCE_OPTION = "force";
    public static final String SIMULATION_NAME_OPTION = "name";
    public static final String SEED_OPTION = "seed";
    public static final String COMMENT_OPTION = "comment";

    private Options options;

    @SuppressWarnings({"AccessStaticViaInstance"})
    public ArgumentParser() {
        options = new Options();
        options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("path to a parameter file").isRequired().create(PARAMETERIZATION_OPTION));
        options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("path to a result config file").isRequired().create(RESULT_CONFIGURATION_OPTION));
        options.addOption(OptionBuilder.withArgName("n").hasArg().withDescription("number of iterations").isRequired().create(ITERATIONS_OPTION));

        options.addOption(OptionBuilder.hasArg(false).withDescription("force import").create(FORCE_OPTION));
        options.addOption(OptionBuilder.withArgName("name").hasArg().withDescription("name of the simulation").create(SIMULATION_NAME_OPTION));
        options.addOption(OptionBuilder.withArgName("comment").hasArg().withDescription("simulation comment").create(COMMENT_OPTION));
        options.addOption(OptionBuilder.withArgName("seed").hasArg().withDescription("simulation seed").create(SEED_OPTION));

        OptionGroup outputStrategy = new OptionGroup();
        outputStrategy.addOption(OptionBuilder.hasArg(false).withDescription("no results will be persisted").create(NO_OUTPUT_OPTION));
        outputStrategy.addOption(OptionBuilder.withArgName("path").hasArg().withDescription("results will be persisted to a file").create(FILE_OUTPUT_OPTION));
        outputStrategy.addOption(OptionBuilder.hasArg(false).withDescription("results will be persisted to the database").create(DB_OUTPUT_OPTION));
        outputStrategy.setRequired(true);
        options.addOptionGroup(outputStrategy);

        LOG.debug("Parser options created: " + options.toString());
    }

    public void printHelp(PrintWriter out) {
        new HelpFormatter().printHelp(out, 200, "...RunSimulation [options]", "\n", options, 10, 20, "", false);
        out.flush();
    }

    public CommandLine parseArguments(String[] args) {
        try {
            return new GnuParser().parse(options, args);
        } catch (ParseException e) {
            LOG.error("Failed to parse arguments. Reason: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return null;
        }
    }

    public ICollectorOutputStrategy getOutputStrategy(CommandLine commandLine) {
        if (commandLine.hasOption(DB_OUTPUT_OPTION)) {
            return new DBOutput();
        } else if (commandLine.hasOption(FILE_OUTPUT_OPTION)) {
            FileOutput output = new FileOutput();
            output.setResultLocation(commandLine.getOptionValue(FILE_OUTPUT_OPTION));
            return output;
        } else {
            return new NoOutput();
        }
    }

    public Simulation createSimulation(CommandLine commandLine) throws Exception {
        final boolean force = commandLine.hasOption(FORCE_OPTION);

        String simulationName = "CLI " + new DateTime().toString();
        if (commandLine.hasOption(SIMULATION_NAME_OPTION)) {
            simulationName = commandLine.getOptionValue(SIMULATION_NAME_OPTION);
        }

        final Simulation simulation = new Simulation(simulationName);
        Parameterization parameterization = getParameterization(commandLine.getOptionValue(PARAMETERIZATION_OPTION), force);
        ResultConfiguration resultConfiguration = getResultConfiguration(commandLine.getOptionValue(RESULT_CONFIGURATION_OPTION), force);
        if (!parameterization.getModelClass().getName().equals(resultConfiguration.getModelClass().getName())) {
            throw new IllegalStateException("Parameterization has model class " + parameterization.getModelClass().getName() + " but result config has " + resultConfiguration.getModelClass().getName());
        }
        simulation.setModelClass(parameterization.getModelClass());
        simulation.setParameterization(parameterization);
        simulation.setTemplate(resultConfiguration);
        simulation.setNumberOfIterations(Integer.parseInt(commandLine.getOptionValue(ITERATIONS_OPTION)));
        simulation.setPeriodCount(parameterization.getPeriodCount());
        simulation.setModelVersionNumber(new VersionNumber("1"));
        if (commandLine.hasOption(SEED_OPTION)) {
            simulation.setRandomSeed(Integer.parseInt(commandLine.getOptionValue(SEED_OPTION)));
        } else {
            long millis = System.currentTimeMillis();
            long millisE5 = (long) (millis / 1E5);
            simulation.setRandomSeed((int) (millis - millisE5 * 1E5));
        }
        if (commandLine.hasOption(COMMENT_OPTION)) {
            simulation.setComment(commandLine.getOptionValue(COMMENT_OPTION));
        }
        simulation.save();
        return simulation;
    }

    private Parameterization getParameterization(String fileName, boolean force) throws Exception {
        File file = new File(fileName);
        ConfigObject configObject = new ConfigSlurper().parse(file.toURI().toURL());

        final String name = file.getName();
        Parameterization parameterization = ParameterizationHelper.createParameterizationFromConfigObject(configObject, name.substring(0, name.indexOf(".groovy")));
        final Parameterization newestExistingParameterization = findNewestVersion(parameterization);
        if (force) {
            LOG.info("Force flag set. Importing new version.");
            if (newestExistingParameterization != null) {
                final VersionNumber versionNumber = VersionNumber.incrementVersion(newestExistingParameterization);
                parameterization.setVersionNumber(versionNumber);
            }
            parameterization.save();
        } else {
            LOG.info("Force flag not set. Trying to reuse existing parameterization.");
            if (newestExistingParameterization == null) {
                LOG.info("Existing parameterization does not exist. Importing new version.");
                parameterization.save();
            } else {
                if (ItemComparator.contentEquals(newestExistingParameterization, parameterization)) {
                    LOG.info("Source parameterization is equal to existing parameterization. Re-using " + newestExistingParameterization.getName() + " " + newestExistingParameterization.getVersionNumber().toString());
                    parameterization = newestExistingParameterization;
                } else {
                    LOG.info("Existing parameterization exists but is different. Importing new version");
                    final VersionNumber versionNumber = VersionNumber.incrementVersion(newestExistingParameterization);
                    parameterization.setVersionNumber(versionNumber);
                    parameterization.save();
                }
            }

        }

        return parameterization;
    }

    private ResultConfiguration getResultConfiguration(String fileName, boolean force) throws Exception {
        File file = new File(fileName);
        ConfigObject configObject = new ConfigSlurper().parse(file.toURI().toURL());

        final String name = file.getName();
        ResultConfiguration resultConfiguration = new ResultConfiguration(configObject, name.substring(0, name.indexOf(".groovy")));
        final ResultConfiguration newestExistingResultConfiguration = findNewestVersion(resultConfiguration);
        if (force) {
            LOG.info("Force flag set. Importing new version.");
            if (newestExistingResultConfiguration != null) {
                final VersionNumber versionNumber = VersionNumber.incrementVersion(newestExistingResultConfiguration);
                resultConfiguration.setVersionNumber(versionNumber);
            }
            resultConfiguration.save();
        } else {
            LOG.info("Force flag not set. Trying to reuse existing result configuration.");
            if (newestExistingResultConfiguration == null) {
                LOG.info("Existing  result configuration does not exist. Importing new version.");
                resultConfiguration.save();
            } else {
                if (ItemComparator.contentEquals(newestExistingResultConfiguration, resultConfiguration)) {
                    LOG.info("Source result configuration is equal to existing  result configuration. Re-using " + newestExistingResultConfiguration.getName() + " " + newestExistingResultConfiguration.getVersionNumber().toString());
                    resultConfiguration = newestExistingResultConfiguration;
                } else {
                    LOG.info("Existing result configuration exists but is different. Importing new version");
                    final VersionNumber versionNumber = VersionNumber.incrementVersion(newestExistingResultConfiguration);
                    resultConfiguration.setVersionNumber(versionNumber);
                    resultConfiguration.save();
                }
            }

        }

        return resultConfiguration;
    }

    private Parameterization findNewestVersion(Parameterization parameterization) {
        List<VersionNumber> versionNumbers = VersionNumber.getExistingVersions(parameterization);
        Parameterization newestParameterization = null;
        if (!versionNumbers.isEmpty()) {
            Collections.sort(versionNumbers);
            newestParameterization = new Parameterization(parameterization.getName());
            VersionNumber newestVersion = versionNumbers.get(versionNumbers.size() - 1);

            newestParameterization.setModelClass(parameterization.getModelClass());
            newestParameterization.setVersionNumber(newestVersion);
            newestParameterization.load(true);
        }

        return newestParameterization;
    }

    private ResultConfiguration findNewestVersion(ResultConfiguration resultConfiguration) {
        List<VersionNumber> versionNumbers = VersionNumber.getExistingVersions(resultConfiguration);
        ResultConfiguration newestResultConfiguration = null;
        if (!versionNumbers.isEmpty()) {
            Collections.sort(versionNumbers);
            newestResultConfiguration = new ResultConfiguration(resultConfiguration.getName());
            VersionNumber newestVersion = versionNumbers.get(versionNumbers.size() - 1);

            newestResultConfiguration.setModelClass(resultConfiguration.getModelClass());
            newestResultConfiguration.setVersionNumber(newestVersion);
            newestResultConfiguration.load(true);
        }

        return newestResultConfiguration;
    }
}
