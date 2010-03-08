package org.pillarone.riskanalytics.core.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService;
import org.pillarone.riskanalytics.core.initialization.GrailsEnvironment;
import org.pillarone.riskanalytics.core.output.NoOutput;
import org.pillarone.riskanalytics.core.simulation.engine.RunSimulationService;
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration;
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner;
import org.pillarone.riskanalytics.core.simulation.item.Simulation;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RunSimulation {

    private static Log LOG = LogFactory.getLog(RunSimulation.class);

    public static void main(String[] args) throws Exception {
        try {
            System.setProperty("skipImport", "true");
            GrailsEnvironment.setUp();
            ArgumentParser parser = new ArgumentParser();
            CommandLine commandLine = parser.parseArguments(args);
            if (commandLine == null) {
                StringWriter writer = new StringWriter();
                parser.printHelp(new PrintWriter(writer));
                LOG.error(writer.toString());
                shutdown();
                return;
            }

            SimulationRunner runner = SimulationRunner.createRunner();
            SimulationConfiguration configuration = createConfiguration(parser, commandLine);

            ImportStructureInTransaction.importStructure(configuration);

            RunSimulationService.getService().runSimulation(runner, configuration);
            SimulationLogger logger = new SimulationLogger(runner, LOG);
            synchronized (logger) {
                logger.wait();
            }
            LOG.info("Simulation completed with simulation run id: " + runner.getCurrentScope().getSimulation().id);
        } finally {
            shutdown();
        }
    }


    private static SimulationConfiguration createConfiguration(ArgumentParser parser, CommandLine commandLine) throws Exception {
        SimulationConfiguration configuration = new SimulationConfiguration();
        configuration.setOutputStrategy(parser.getOutputStrategy(commandLine));
        Simulation simulation = parser.createSimulation(commandLine);
        simulation.getParameterization().load();
        simulation.getTemplate().load();
        configuration.setSimulation(simulation);
        return configuration;
    }

    private static void shutdown() {
        AbstractApplicationContext applicationContext = (AbstractApplicationContext) ApplicationHolder.getApplication().getMainContext();
        applicationContext.close();
        try {
            StdSchedulerFactory.getDefaultScheduler().shutdown();
        } catch (SchedulerException e) {
            LOG.warn("Error shutting down quartz scheduler", e);
        }
    }
}

