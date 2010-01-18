package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.mock.interceptor.StubFor
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.output.CollectorFactory
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class WireModelActionTests extends GroovyTestCase {

    void testProtocol() {

        StubFor modelStub = new StubFor(EmptyModel)
        StubFor scopeStub = new StubFor(SimulationScope)
        StubFor parameterizationHelperStub = new StubFor(ParameterizationHelper)
        StubFor parameterApplicatorStub = new StubFor(ParameterApplicator)

        scopeStub.demand.getModel {new EmptyModel()}
        scopeStub.demand.getParameters {new ParameterizationDAO()}
        scopeStub.demand.getResultConfiguration {return new ResultConfigurationDAO(collectorInformation: [])}
        scopeStub.demand.getParameterApplicator {new ParameterApplicator()}
        parameterApplicatorStub.demand.applyParameterForPeriod {period ->}
        modelStub.demand.wire {}
        scopeStub.demand.getCollectorFactory {return new CollectorFactory()}
        scopeStub.demand.getStructureInformation {return null}
        modelStub.demand.optimizeComposedComponentWiring {}

        modelStub.use {
            scopeStub.use {
                parameterizationHelperStub.use {
                    parameterApplicatorStub.use {
                        SimulationScope simulationScope = new SimulationScope()
                        Action wireModelAction = new WireModelAction(simulationScope: simulationScope)
                        wireModelAction.perform()
                    }
                }
            }
        }
    }
}