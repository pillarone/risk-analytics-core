package org.pillarone.riskanalytics.core.simulation.engine.grid

import grails.util.Holders
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.gridgain.grid.GridJobAdapterEx
import org.joda.time.DateTimeZone
import org.pillarone.riskanalytics.core.components.ResourceRegistry
import org.pillarone.riskanalytics.core.output.aggregation.IPacketAggregator
import org.pillarone.riskanalytics.core.output.aggregation.PacketAggregatorRegistry
import org.pillarone.riskanalytics.core.simulation.ILimitedPeriodCounter
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.engine.SimulationConfiguration
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.GridOutputStrategy
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.JobResult
import org.pillarone.riskanalytics.core.simulation.item.Resource
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.support.GenericApplicationContext

@CompileStatic
class SimulationJob extends GridJobAdapterEx {

    protected SimulationConfiguration simulationConfiguration
    private SimulationRunner runner = SimulationRunner.createRunner()
    private UUID jobIdentifier
    private int jobCount = 0;

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
            initSpringContext()
/** Setting the default time zone to UTC avoids problems in multi user context with different time zones
 *  and switches off daylight saving capabilities and possible related problems.                */
            DateTimeZone.default = DateTimeZone.UTC
            TimeZone.default = TimeZone.getTimeZone("UTC")

            //***** https://issuetracking.intuitive-collaboration.com/jira/browse/KTI-15
            getClass().classLoader.loadClass("java.lang.Character")
            getClass().classLoader.loadClass("java.lang.reflect.InvocationTargetException")
            getClass().classLoader.loadClass("org.pillarone.riskanalytics.core.components.ComponentUtils")
            //***** http://www.gridgainsystems.com/jiveforums/thread.jspa?threadID=1324&tstart=0

            for (Map.Entry<Class, IPacketAggregator> entry in aggregatorMap.entrySet()) {
                PacketAggregatorRegistry.registerAggregator(entry.key, entry.value)
            }
            ResourceRegistry.clear()
            ResourceRegistry.preLoad(loadedResources)

            ExpandoMetaClass.enableGlobally()
            runner.jobCount = jobCount
            runner.simulationConfiguration = simulationConfiguration
            runner.start()

            GridOutputStrategy outputStrategy = (GridOutputStrategy) this.simulationConfiguration.outputStrategy
            final JobResult result = new JobResult(
                    totalMessagesSent: outputStrategy.totalMessages, start: start, end: new Date(),
                    nodeName: jobIdentifier.toString(), simulationException: runner.error?.error,
                    completedIterations: runner.currentScope.iterationsDone
            )
            final IPeriodCounter periodCounter = runner.currentScope.iterationScope.periodScope.periodCounter
            if (periodCounter instanceof ILimitedPeriodCounter) {
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

    private void initSpringContext() {
        if (Holders.grailsApplication == null) { //if not on the main node, create dummy application with required beans
            GenericApplicationContext ctx = new GenericApplicationContext()
            for (Map.Entry<String, BeanDefinition> definition in simulationConfiguration.beans.entrySet()) {
                ctx.registerBeanDefinition(definition.key, definition.value)
            }
            DefaultGrailsApplication grailsApplication = new DefaultGrailsApplication()
            grailsApplication.mainContext = ctx
            Holders.grailsApplication = grailsApplication
        }
    }
}
