package org.pillarone.riskanalytics.core.simulation.engine

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.batch.BatchRunInfoService
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.springframework.transaction.TransactionStatus
import org.pillarone.riskanalytics.core.simulation.engine.actions.*
import org.joda.time.DateTime

/**
 * This is the main entity to run a simulation. To do this, create a runner object (SimulationRunner.createRunner()).
 * A simulation run is performed in 3 different phases. These 3 phases are executed by performing
 *
 * - the preSimulationActions, where the model is initialized and wired, parameters are loaded and prepared,
 * the collector instances get created and attached to the model. The different task are implemented as Actions.
 *
 * - simulationAction, where the iterative simulation is done
 *
 * - the postSimulationActions, where any operations are done to finish a simulation
 * e.g. uploading the batch file for persistence, doing postSimulationCalculation.
 * The different task are implemented as Actions
 *
 *
 * A SimulationRunner instance has to be configured with a SimulationConfiguration before starting a simulation.
 * Each simulation has to use a new SimulationRunner instance.
 */

public class SimulationRunner {

    private static Log LOG = LogFactory.getLog(SimulationRunner)
    private long start

    List preSimulationActions = []
    SimulationAction simulationAction
    List postSimulationActions = []

    SimulationScope currentScope

    SimulationError error

    BatchRunInfoService batchRunInfoService

    /**
     * Starting a simulation run by performing the
     *
     * - preSimulationActions,
     * - the simulationAction and
     * - the postSimulationActions
     *
     * Any exception occuring during the simulation is caught and the error object will be initialized.
     */
    public void start() {
        simulationState = SimulationState.INITIALIZING
        notifySimulationStateChanged(currentScope?.simulation, simulationState)
        LOG.debug "start simulation"
        start = System.currentTimeMillis()
        DateTime startDate = new DateTime(start)
        currentScope?.simulation?.start = startDate
        try {
            for (Action action in preSimulationActions) {
                if (!performAction(action, null)) {
                    deleteCancelledSimulation()
                    return
                }
            }
            long initializationTime = System.currentTimeMillis() - start
            LOG.info "Initialization completed in ${initializationTime}ms"
            notifySimulationStateChanged(currentScope?.simulation, SimulationState.RUNNING)

            boolean shouldReturn = false
            //Transaction is necessary because PathMappings etc might be inserted when writing bulk insert files
            SimulationRun.withTransaction {TransactionStatus status ->
                if (!performAction(simulationAction, SimulationState.RUNNING)) {
                    deleteCancelledSimulation()
                    shouldReturn = true
                }
            }
            if (shouldReturn) return
            LOG.info "${currentScope?.numberOfIterations} iterations completed in ${System.currentTimeMillis() - (start + initializationTime)}ms"
            notifySimulationStateChanged(currentScope?.simulation, SimulationState.POST_SIMULATION_CALCULATIONS)
            //Transaction because of saving results to db
            SimulationRun.withTransaction {TransactionStatus status ->
                for (Action action in postSimulationActions) {
                    if (!performAction(action, null)) {
                        shouldReturn = true
                    }
                }
            }
            if (shouldReturn) {
                deleteCancelledSimulation()
                return
            }

        } catch (Throwable t) {
            notifySimulationStateChanged(currentScope?.simulation, SimulationState.ERROR)
            simulationState = SimulationState.ERROR
            error = new SimulationError(
                    simulationRunID: currentScope.simulation?.id,
                    iteration: currentScope.iterationScope.currentIteration,
                    period: currentScope.iterationScope.periodScope.currentPeriod,
                    error: t
            )
            LOG.error this, t
            LOG.debug error.dump()
            deleteFailedSimulation()
            return
        }
        if (simulationAction.isCancelled()) {
            deleteCancelledSimulation()
            return
        }
        LOG.debug "end simulation"
        long end = System.currentTimeMillis()
        currentScope?.simulation?.end = new DateTime(end)
        currentScope?.simulation?.save()

        LOG.info "simulation took ${end - start} ms"
        simulationState = simulationAction.isStopped() ? SimulationState.STOPPED : SimulationState.FINISHED
        notifySimulationStateChanged(currentScope?.simulation, simulationState)
        cleanup()
    }

    private void deleteFailedSimulation() {
        try {
            LOG.info "failed simulation ${currentScope.simulation.name} will be deleted"
            currentScope.simulation.delete()
        } catch (Exception ex) {
            LOG.error "failed deleting simulation ${currentScope.simulation.name}", ex
        }
        cleanup()
    }

    private void deleteCancelledSimulation() {
        if (simulationAction.isCancelled()) {
            LOG.info "canceled simulation ${currentScope.simulation.name} will be deleted"
            currentScope.simulation.delete()
        }
        cleanup()
    }

    /**
     * The current simulation will be stopped at the next iteration. The simulationState will indicate, that
     * the simulation has been stopped.
     */
    public void stop() {
        LOG.info("Simulation stopped by user")
        simulationAction.stop()
        simulationState = SimulationState.STOPPED
    }

    public synchronized void cancel() {
        LOG.info("Simulation cancelled by user")
        simulationAction.cancel()
        simulationState = SimulationState.CANCELED
    }

    protected boolean performAction(Action action, SimulationState newState) {
        LOG.info "Trying to perform action ${action.class.simpleName}..."
        synchronized (this) {
            if (simulationAction.isCancelled()) {
                LOG.info "Action aborted because simulation is cancelled"
                return false
            }
            if (newState != null) {
                simulationState = newState
            }
        }
        action.perform()
        return true
    }


    DateTime getEstimatedSimulationEnd() {
        int progress = currentScope.getProgress()
        if (progress > 0 && simulationState == SimulationState.RUNNING) {
            long now = System.currentTimeMillis()
            long onePercentTime = (now - start) / progress
            long estimatedEnd = now + (onePercentTime * (100 - progress))
            return new DateTime(estimatedEnd)
        } else if (simulationState == SimulationState.POST_SIMULATION_CALCULATIONS) {
            CalculatorAction action = postSimulationActions.find { it instanceof CalculatorAction }
            return action?.calculator?.estimatedEnd
        }
        return null
    }

    int getProgress() {
        if (simulationState == SimulationState.POST_SIMULATION_CALCULATIONS) {
            CalculatorAction action = postSimulationActions.find { it instanceof CalculatorAction }
            return action?.calculator?.progress
        } else {
            return currentScope.progress
        }
    }

    /**
     * Configure the runner with the passed configuration.
     * All information about the simulation will be gathered from the configuration and the actions and scopes get the requiered parameter.
     */
    public void setSimulationConfiguration(SimulationConfiguration configuration) {
        Simulation simulation = (configuration.simulation)
        currentScope.simulation = simulation
        currentScope.model = simulation.modelClass.newInstance()
        currentScope.outputStrategy = configuration.outputStrategy
        currentScope.iterationScope.numberOfPeriods = simulation.periodCount

        simulationAction.iterationAction.periodAction.model = currentScope.model

        currentScope.mappingCache = MappingCache.instance
    }

    /**
     * Create a new instance for running a simulation. All Actions and Scopes get created.
     * The runner instance has to be configured with a SimulationConfiguration before being able to run.
     */
    public static SimulationRunner createRunner() {

        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope: periodScope)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope)


        Action initModel = new InitModelAction(simulationScope: simulationScope)
        Action randomSeed = new RandomSeedAction(simulationScope: simulationScope)
        Action initParams = new PrepareParameterizationAction(simulationScope: simulationScope, periodScope: periodScope)
        Action runtimeParams = new InjectRuntimeParameterAction(simulationScope: simulationScope)
        Action applyGlobalParams = new ApplyGlobalParametersAction(simulationScope: simulationScope)
        Action prepareStructure = new PrepareStructureInformationAction(simulationScope: simulationScope)
        Action injectResourceParams = new PrepareResourceParameterizationAction(simulationScope: simulationScope)
        Action wireModel = new WireModelAction(simulationScope: simulationScope)
        Action periodCounter = new CreatePeriodCounterAction(simulationScope: simulationScope)
        Action injectScopes = new InjectScopesAction(
                simulationScope: simulationScope,
                iterationScope: iterationScope,
                periodScope: periodScope
        )

        PeriodAction periodAction = new PeriodAction(periodScope: periodScope, model: simulationScope.model)
        IterationAction iterationAction = new IterationAction(periodAction: periodAction, iterationScope: iterationScope)
        SimulationAction simulationAction = new SimulationAction(iterationAction: iterationAction, simulationScope: simulationScope)

        Action calculatorAction = new CalculatorAction(simulationScope: simulationScope)
        Action finishOutputAction = new FinishOutputAction(simulationScope: simulationScope)

        SimulationRunner runner = new SimulationRunner()

        //The order of the pre & post simulation actions is important.
        // WireModelAction must be before CreatePeriodCounterAction
        // PrepareStructureInformationAction must be before WireModelAction
        runner.preSimulationActions << initModel
        runner.preSimulationActions << randomSeed
        //order important!
        runner.preSimulationActions << initParams //creates resource params
        runner.preSimulationActions << runtimeParams //inject runtime params - resource params must be ready
        runner.preSimulationActions << applyGlobalParams //distribute global params - resource params must already be injected
        runner.preSimulationActions << prepareStructure
        runner.preSimulationActions << injectResourceParams
        runner.preSimulationActions << wireModel
        runner.preSimulationActions << periodCounter
        runner.preSimulationActions << injectScopes

        runner.simulationAction = simulationAction

        runner.postSimulationActions << finishOutputAction
        runner.postSimulationActions << calculatorAction

        runner.currentScope = simulationScope

        return runner
    }

    public SimulationState getSimulationState() {
        return currentScope.simulationState
    }

    protected void setSimulationState(SimulationState newState) {
        currentScope.simulationState = newState
    }

    protected void notifySimulationStateChanged(Simulation simulation, SimulationState simulationState) {
        batchRunInfoService?.batchSimulationStateChanged(simulation, simulationState)
    }

    //cleanup

    protected void cleanup() {
        WiringUtils.forAllComponents(currentScope.model) {originName, Component component ->
            clearScope "simulationScope", component
            clearScope "iterationScope", component
            clearScope "periodScope", component
            component.clearPropertyCache()

            component.allOutputTransmitter.each {ITransmitter transmitter ->
                if (transmitter.receiver instanceof PacketCollector) {
                    clearScope "simulationScope", transmitter.receiver
                    clearScope "iterationScope", transmitter.receiver
                    clearScope "periodScope", transmitter.receiver
                    transmitter.receiver.clearPropertyCache()
                }
            }

            clearStore(component)
            currentScope.parameters.parameterHolders*.clearCachedValues()

        }
    }

    private void clearStore(Component component) {
        Set<String> propertyNames = GroovyUtils.getProperties(component).keySet()
        if (propertyNames.contains('periodStore')) {
            component.periodStore = null
        }
        if (propertyNames.contains('iterationStore')) {
            component.iterationStore = null
        }
    }

    private void clearScope(String scopeName, Component component) {
        if (GroovyUtils.getProperties(component).keySet().contains(scopeName)) {
            component[scopeName] = null
        }
    }

}
