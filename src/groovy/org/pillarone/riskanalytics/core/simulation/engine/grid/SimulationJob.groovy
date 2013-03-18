package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gridgain.grid.GridJobAdapterEx
import org.pillarone.riskanalytics.core.output.aggregation.PacketAggregatorRegistry
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.GridOutputStrategy
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.JobResult
import org.pillarone.riskanalytics.core.output.aggregation.IPacketAggregator
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.ILimitedPeriodCounter
import org.pillarone.riskanalytics.core.components.ResourceRegistry
import org.pillarone.riskanalytics.core.simulation.item.Resource
import org.springframework.context.support.GenericApplicationContext
import org.springframework.beans.factory.config.BeanDefinition
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.joda.time.DateTimeZone

class SimulationJob extends GridJobAdapterEx {

    private SimulationConfiguration simulationConfiguration
    private SimulationRunner runner = SimulationRunner.createRunner()
    private UUID jobIdentifier
    private int jobCount = 0;

    private static Log LOG = LogFactory.getLog(SimulationJob)

    Map<Class, IPacketAggregator> aggregatorMap = [:]
    List<Resource> loadedResources = []

    public SimulationJob(SimulationConfiguration simulationConfiguration, UUID jobId, UUID masterNodeId) {
        this.jobIdentifier = jobId
        this.simulationConfiguration = simulationConfiguration
        this.simulationConfiguration.outputStrategy = new GridOutputStrategy(masterNodeId, runner, jobIdentifier);
    }

    JobResult execute() {
        Date start = new Date()

        try {
/** Setting the default time zone to UTC avoids problems in multi user context with different time zones
 *  and switches off daylight saving capabilities and possible related problems.                */
            DateTimeZone.setDefault(DateTimeZone.UTC)
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

            //***** https://issuetracking.intuitive-collaboration.com/jira/browse/KTI-15
            getClass().getClassLoader().loadClass("java.lang.Character")
            getClass().getClassLoader().loadClass("java.lang.reflect.InvocationTargetException")
            getClass().getClassLoader().loadClass("org.pillarone.riskanalytics.core.components.ComponentUtils")
            //***** http://www.gridgainsystems.com/jiveforums/thread.jspa?threadID=1324&tstart=0

            for(Map.Entry<Class, IPacketAggregator> entry in aggregatorMap) {
                PacketAggregatorRegistry.registerAggregator(entry.key, entry.value)
            }
            ResourceRegistry.clear()
            ResourceRegistry.preLoad(loadedResources)

            initSpringContext()
            ExpandoMetaClass.enableGlobally()
            runner.setJobCount(jobCount)
            runner.setSimulationConfiguration(simulationConfiguration)
            runner.start()

            GridOutputStrategy outputStrategy = this.simulationConfiguration.outputStrategy
            final JobResult result = new JobResult(
                    totalMessagesSent: outputStrategy.totalMessages, start: start, end: new Date(),
                    nodeName: jobIdentifier.toString(), simulationException: runner.error?.error,
                    completedIterations: runner.currentScope.iterationsDone
            )
            final IPeriodCounter periodCounter = runner.currentScope.iterationScope.periodScope.periodCounter
            if(periodCounter instanceof ILimitedPeriodCounter) {
                result.numberOfSimulatedPeriods = periodCounter.periodCount()
            } else {
                result.numberOfSimulatedPeriods = simulationConfiguration.simulation.periodCount
            }
            return result
        } catch (Throwable e) {
            return new JobResult(
                    totalMessagesSent: 0, start: start, end: new Date(),
                    nodeName: jobIdentifier.toString(), simulationException: e,
                    completedIterations: runner.currentScope.iterationsDone
            )
        }
    }

    void cancel() {
        runner.cancel()
        super.cancel()
    }

    void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }


    UUID getJobIdentifier() {
        return jobIdentifier
    }

    private void initSpringContext() {
        if (ApplicationHolder.application == null) { //if not on the main node, create dummy application with required beans
            GenericApplicationContext ctx = new GenericApplicationContext()
            for (Map.Entry<String, BeanDefinition> definition in simulationConfiguration.beans.entrySet()) {
                ctx.registerBeanDefinition(definition.key, definition.value)
            }
            DefaultGrailsApplication grailsApplication = new DefaultGrailsApplication()
            grailsApplication.mainContext = ctx
            ApplicationHolder.application = grailsApplication
        }
    }

}
