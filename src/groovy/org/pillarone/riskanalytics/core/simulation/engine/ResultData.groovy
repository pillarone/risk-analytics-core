package org.pillarone.riskanalytics.core.simulation.engine

import groovy.transform.CompileStatic
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
import org.pillarone.riskanalytics.core.simulation.item.SimulationProfile
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

class ResultData {

    private static final Log LOG = LogFactory.getLog(ResultData)

    Map<DataSourceDefinition, List<ExternalPacket>> cache = new HashMap()

    void load(List<DataSourceDefinition> definitions) {
        for(DataSourceDefinition definition in definitions) {

            List<ExternalPacket> list = []

            definition.parameterization.load(false)
            SimulationRun run = SimulationRun.findByParameterization(definition.parameterization.dao) //TODO: compare runtime params
            if(run == null) {
                throw new IllegalArgumentException("No matching result found!")
            }

            for(String field in definition.fields) {
                for(int period in definition.periods) {
                    try {
                        long time = System.currentTimeMillis()
                        IterationFileAccessor ifa = ResultAccessor.createFileAccessor(run, definition.path, field, definition.collectorName, period)
                        while(ifa.fetchNext()) {
                            if (definition.collectorName == AggregatedCollectingModeStrategy.IDENTIFIER) {
                                list << new AggregatedExternalPacket(
                                        basedOn: definition, field: field, iteration: ifa.iteration, period: period,
                                        value: ifa.value
                                )
                            } else if(definition.collectorName == SingleValueCollectingModeStrategy.IDENTIFIER) {
                                list << new SingleExternalPacket(
                                        basedOn: definition, field: field, iteration: ifa.iteration, period: period,
                                        values: ifa.singleValues
                                )
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
}
