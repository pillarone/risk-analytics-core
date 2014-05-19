package org.pillarone.riskanalytics.core.simulation.engine

import groovy.transform.CompileStatic
import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.components.DataSourceDefinition
import org.pillarone.riskanalytics.core.dataaccess.IterationFileAccessor
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy
import org.pillarone.riskanalytics.core.packets.AggregatedExternalPacket
import org.pillarone.riskanalytics.core.packets.ExternalPacket
import org.pillarone.riskanalytics.core.packets.SingleExternalPacket
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder

class ResultData {

    private static final Log LOG = LogFactory.getLog(ResultData)

    Map<DataSourceDefinition, List<ExternalPacket>> cache = new HashMap()

    void load(List<DataSourceDefinition> definitions, Simulation simulation) {
        for(DataSourceDefinition definition in definitions) {

            List<ExternalPacket> list = []

            definition.parameterization.load(false)
            List<SimulationRun> candidates = SimulationRun.findAllWhere(
                    parameterization: definition.parameterization.dao,
                    iterations: simulation.numberOfIterations,
                    randomSeed: simulation.randomSeed,
            )

            SimulationRun run = null

            for(SimulationRun simulationRun in candidates) {
                Simulation candidate = new Simulation(simulationRun.name)
                candidate.modelClass = simulation.modelClass
                candidate.load()

                boolean valid = true
                for(ParameterHolder holder in simulation.runtimeParameters) {
                    if(!(holder.businessObject == candidate.runtimeParameters.find { it.path == holder.path}?.businessObject)) {
                        valid = false
                    }
                }

                if(valid) {
                    run = simulationRun
                    break
                }

            }
            if(run == null) {
                throw new IllegalArgumentException("No matching result found!")
            }

            for(int period in definition.periods) {

                Map<IterationPathPair, ExternalPacket> packets = [:]

                for(String field in definition.fields) {
                    try {
                        long time = System.currentTimeMillis()
                        IterationFileAccessor ifa = ResultAccessor.createFileAccessor(run, definition.path, field, definition.collectorName, period)
                        while(ifa.fetchNext()) {
                            if (definition.collectorName == AggregatedCollectingModeStrategy.IDENTIFIER) {

                                IterationPathPair pair = new IterationPathPair(ifa.iteration, definition.path)
                                AggregatedExternalPacket packet = packets.get(pair)

                                if(packet == null) {
                                    packet = new AggregatedExternalPacket(basedOn: definition, iteration: ifa.iteration, period: period)
                                    packet.addValue(field, ifa.value)

                                    packets.put(pair, packet)
                                    list << packet
                                } else {
                                    packet.addValue(field, ifa.value)
                                }


                            } else if(definition.collectorName == SingleValueCollectingModeStrategy.IDENTIFIER) {
                                IterationPathPair pair = new IterationPathPair(ifa.iteration, definition.path)
                                SingleExternalPacket packet = packets.get(pair)

                                if(packet == null) {
                                    packet = new SingleExternalPacket(basedOn: definition, iteration: ifa.iteration, period: period)
                                    packet.addValue(field, ifa.singleValues)

                                    packets.put(pair, packet)
                                    list << packet
                                } else {
                                    packet.addValue(field, ifa.singleValues)
                                }
                            } else {
                                throw new IllegalStateException("Unsupported collector for external data: ${definition.collectorName}")
                            }
                        }
                        LOG.info("External data for ${definition} loaded in ${System.currentTimeMillis() - time}ms")
                    } catch (Exception e) {
                        throw  new IllegalStateException("No results found in ${run.name} for ${definition.path} / $field / $period")
                    }
                }
            }

            String name = definition.parameterization.name
            VersionNumber number = definition.parameterization.versionNumber
            Class modelClass = definition.parameterization.modelClass

            definition.parameterization = new Parameterization(name, modelClass)
            definition.parameterization.versionNumber = number

            cache.put(definition, list)

        }
    }

    @CompileStatic
    List<ExternalPacket> getValuesForDefinition(DataSourceDefinition definition) {
        return cache[definition]
    }

    @CompileStatic
    private static class IterationPathPair {

        int iteration
        String path

        IterationPathPair(int iteration, String path) {
            this.iteration = iteration
            this.path = path
        }

        @Override
        int hashCode() {
            return new HashCodeBuilder().append(iteration).append(path).toHashCode()
        }

        @Override
        boolean equals(Object obj) {
            if(obj instanceof IterationPathPair) {
                return new EqualsBuilder().append(iteration, obj.iteration).append(path, obj.path).equals
            }

            return false
        }
    }
}
