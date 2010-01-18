package org.pillarone.riskanalytics.core.model

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.components.PeriodStore
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils

abstract class Model {

    List<Component> allComponents = []
    List<ComposedComponent> allComposedComponents = []

    private List<Component> startComponents = []
    protected List<PeriodStore> allPeriodStores = []
    private List<Component> immutableStartComponents = startComponents.asImmutable()

    public String getName() {
        return this.getClass().simpleName - "Model"
    }

    void init() {
        allComponents.clear()
        allComposedComponents.clear()
        startComponents.clear()

        initComponents()
        initAllComponents()
    }

    abstract void initComponents()

    void initAllComponents() {
        for (prop in this.properties) {
            if (prop.value instanceof Component && !allComponents.contains(prop.value)) {
                allComponents << prop.value
            }
            if (prop.value instanceof ComposedComponent && !allComposedComponents.contains(prop.value)) {
                allComposedComponents << prop.value
            }
        }
    }

    /**
     *  Wires the model and its ComposedComponents. Furthermore the
     *  validateWiring() of all components in allComponent is executed.
     */
    // todo(sku): call validateWiring of composed sub components

    void wire() {
        WiringUtils.use(WireCategory) {
            wireComponents()
        }
        traverseSubComponents()
        for (Component component: allComponents) {
            component.validateWiring()
        }
    }

    abstract void wireComponents()

    // todo dk: why is this code disabled?
    /**
     *  Internal wiring of sub components. Implementation detail: If an external port
     *  is not wired, the internal wiring for this port won't be done to make sure
     *  the Component.notifyTransmitter() will work properly.
     */
    /*void wireSubComponents() {
        // TODO (msh): wire composed components together with all components in model subclasses
        allComposedComponents.each { ComposedComponent component ->
            component.internalWiring()
        }
    }*/

    void traverseSubComponents() {
        for (ComposedComponent composedComponent: allComposedComponents) {
            composedComponent.internalWiring()
            composedComponent.internalChannelAllocation()  // MultipleCalculationPhaseComposedComponent
        }
    }

    Class getModelClass() { // used for mockability in SimulationContextTests
        this.class
    }

    void injectComponentNames() {
        injectNames(this)
    }
    // todo : ask DK why this has to be at least protected (private won't work)

    protected void injectNames(def target) {
        WiringUtils.forAllComponents(target) {propertyName, Component component ->
            if (component.name == null) {component.name = propertyName}
        }
    }



    public void optimizeComposedComponentWiring() {
        WiringUtils.forAllComponents(this) {propertyName, Component component ->
            if (component instanceof ComposedComponent) {
                component.optimizeWiring()
            }
        }
    }

    public void clearPeriodStore() {
        for (PeriodStore periodStore in allPeriodStores) {
            periodStore.clear()
        }
    }

    /**
     * Creates a suitable  {@code IPeriodCounter}  for this Model.
     * May be null if the model does not need period information during its run.
     */
    abstract IPeriodCounter createPeriodCounter(DateTime beginOfFirstPeriod)

    /**
     * @return If the model needs a start date set before it can run correctly.
     */
    abstract boolean requiresStartDate()

    protected void addStartComponent(Component startComponent) {
        startComponents << startComponent
        immutableStartComponents = startComponents.asImmutable()
    }

    public List<Component> getStartComponents() {
        return immutableStartComponents
    }

    // todo dk : could this be cached?

    public List<Component> getMarkedComponents(Class markerClass) {
        List lobs = new LinkedList()
        for (component in allComponents) {
            traverseModel(component, lobs, markerClass)
        }
        return lobs
    }

    protected void traverseModel(def c, def lobs, Class clazz) {
        if (clazz.isAssignableFrom(c.class)) {
            lobs << c
        } else if (c instanceof ComposedComponent) {
            c.properties.each {key, val ->
                if (key.startsWith('sub')) {
                    traverseModel(val, lobs, clazz)
                }
            }
        }
    }
}