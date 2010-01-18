package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.output.CollectorInformation
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.StructureInformation

/**
 * The CollectorFactory is resonsible for the creation of PacketCollectors as they are defined in the
 * ResultConfiguration. It hands over the outputStrategy to the PacketCollector instances.
 *
 * As the ResultConfiguration can contains wildcard path information for subComponents of DynamicComposedComponents
 * a special handling of those wilcard paths has to be applied. The wildcard path is translated in a way, that the
 * wildcard element is substituted for all subComponents.
 *
 */
public class CollectorFactory {

    private final ICollectorOutputStrategy outputStrategy
    StructureInformation structureInformation

    public CollectorFactory(ICollectorOutputStrategy outputStrategy) {
        this.outputStrategy = outputStrategy
    }

    /**
     * Create all collectors defined in the ResultConfiguration.
     * Attention: For the special handling of wildcards the model has to be initialized and the parameterization for the
     * simulation has to be applied for one period. This ensures, that also dynamically composes components have their
     * subComponents initialized.
     */
    public List createCollectors(ResultConfigurationDAO resultConfiguration, Model model) {
        return enhanceCollectorInformationSet(resultConfiguration.collectorInformation as List, model).collect {
            createCollector(it)
        }
    }




    protected PacketCollector createCollector(CollectorInformation collectorInformation) {
        PacketCollector collector = new PacketCollector(CollectingModeFactory.getStrategy(collectorInformation.collectingStrategyIdentifier))
        collector.outputStrategy = outputStrategy
        collector.path = collectorInformation.path.pathName

        return collector
    }


    protected List enhanceCollectorInformationSet(List collectorInformations, Model model) {
        LinkedList enhancedCollectorInormation = new LinkedList()
        collectorInformations.each {CollectorInformation collectorInformation ->
            def information = findOrCreateCollectorInformation(collectorInformation, model)
            enhancedCollectorInormation += information
        }
        return enhancedCollectorInormation
    }

    protected def findOrCreateCollectorInformation(CollectorInformation collectorInformation, Model model) {
        def resultingCollectorInformation = collectorInformation

        String[] pathElements = collectorInformation.path.pathName.split("\\:")

        def component = model

        pathElements[1..-2].each {componentName ->
            if (component.properties.keySet().contains(componentName)) {
                component = component[componentName]
            } else {
                if (component instanceof DynamicComposedComponent) {
                    resultingCollectorInformation = resolveWildcardPath(component, collectorInformation, componentName)
                    return // leave the each closure
                } else {
                    Map pathToComponent = structureInformation.componentPaths.inverse()
                    String path = pathElements[0..-2].join(":")
                    if (pathToComponent.containsKey(path)) {
                        component = pathToComponent.get(path)
                    } else {
                        throw new MissingPropertyException(componentName, component.class)
                    }
                }
            }
        }
        return resultingCollectorInformation
    }

    private List resolveWildcardPath(DynamicComposedComponent component, CollectorInformation collectorInformation, String wildCard) {
        List result = []
        component.allSubComponents().each {Component subComponent ->
            String newPath = collectorInformation.path.pathName.replace(wildCard, subComponent.name)
            result << new CollectorInformation(
                    path: new PathMapping(pathName: newPath),
                    collectingStrategyIdentifier: collectorInformation.collectingStrategyIdentifier)
        }
        return result
    }
}
