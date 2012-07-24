package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.mock.interceptor.StubFor
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.output.CollectorFactory
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration

class WireModelActionTests extends GroovyTestCase {

    void testProtocol() {

        StubFor modelStub = new StubFor(EmptyModel)
        StubFor scopeStub = new StubFor(SimulationScope)
        StubFor parameterizationHelperStub = new StubFor(ParameterizationHelper)
        StubFor parameterApplicatorStub = new StubFor(ParameterApplicator)

        scopeStub.demand.getModel {new EmptyModel()}
        scopeStub.demand.getParameters {new Parameterization("p")}
        scopeStub.demand.getResultConfiguration {return new ResultConfiguration("rc")}
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