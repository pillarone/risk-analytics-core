package org.pillarone.riskanalytics.core.model

import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.components.PeriodStore
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils

import java.lang.reflect.Field

abstract class Model {

    List<Component> allComponents = []
    List<ComposedComponent> allComposedComponents = []

    private List<Component> startComponents = []
    protected List<PeriodStore> allPeriodStores = []
    private List<Component> immutableStartComponents = startComponents.asImmutable()

    @CompileStatic
    public String getName() {
        return getName(this.getClass())
    }


    @CompileStatic
    void init() {
        allComponents.clear()
        allComposedComponents.clear()
        startComponents.clear()

        initComponents()
        initAllComponents()
    }

    @CompileStatic
    public static String getName(final Class modelClass) {
        return modelClass.simpleName - "Model"
    }

    @CompileStatic
    public List<Component> getAllComponentsRecursively() {
        List<Component> result = []
        if (allComponents == null) {
            initAllComponents();
        }
        for (Component component : allComponents) {
            addComponentsRecursively(component, result);
        }

        return result
    }

    private void addComponentsRecursively(Component component, List<Component> components) {
        components.add(component);
        if (component instanceof ComposedComponent) {
            for (Component nestedComponent : component.allSubComponents()) {
                addComponentsRecursively(nestedComponent, components);
            }
        }
    }

    abstract void initComponents()

    void initAllComponents() {
        for (prop in GroovyUtils.getProperties(this)) {
            if (prop.value instanceof Component && !allComponents.contains(prop.value)) {
                allComponents << prop.value
            }
            if (prop.value instanceof ComposedComponent && !allComposedComponents.contains(prop.value)) {
                allComposedComponents << prop.value
            }
        }
    }

    @CompileStatic
    void accept(IModelVisitor visitor) {
        visitor.visitModel(this)
        for (Component component in allComponents) {
            ModelPath path = new ModelPath()
            component.accept(visitor, path.append(new ModelPathComponent(component.name, component.class)))
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
        for (Component component : allComponents) {
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
        for (ComposedComponent composedComponent : allComposedComponents) {
            composedComponent.internalWiring()
            composedComponent.internalChannelAllocation()  // MultipleCalculationPhaseComposedComponent
        }
    }

    Class getModelClass() { // used for mockability in SimulationContextTests
        this.class
    }

    @CompileStatic
    void injectComponentNames() {
        injectNames(this)
    }
    // todo : ask DK why this has to be at least protected (private won't work)

    protected void injectNames(def target) {
        WiringUtils.forAllComponents(target) { propertyName, Component component ->
            if (component.name == null) {
                component.name = propertyName
            }
        }
    }



    public void optimizeComposedComponentWiring() {
        WiringUtils.forAllComponents(this) { propertyName, Component component ->
            if (component instanceof ComposedComponent) {
                component.optimizeWiring()
            }
        }
    }

    @CompileStatic
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

    void applyGlobalParameters() {
    }

    void injectResourceParameters() {
    }

    /**
     * @return If the model needs a start date set before it can run correctly.
     */
    abstract boolean requiresStartDate()

    @CompileStatic
    protected void addStartComponent(Component startComponent) {
        startComponents << startComponent
        immutableStartComponents = startComponents.asImmutable()
    }

    @CompileStatic
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
            GroovyUtils.getProperties(c).each { key, val ->
                if (key.startsWith('sub')) {
                    traverseModel(val, lobs, clazz)
                }
            }
        }
    }

    @CompileStatic
    public int maxNumberOfFullyDistinctPeriods() {
        Integer.MAX_VALUE
    }

    @CompileStatic
    public String getDefaultResultConfiguration() {
        return null
    }

    @CompileStatic
    public static VersionNumber getModelVersion(Class modelClass) {
        String versionNumber = "1"
        if (MigratableModel.isAssignableFrom(modelClass)) {
            MigratableModel instance = (MigratableModel) modelClass.newInstance()
            versionNumber = instance.version.toString()
        }
        return new VersionNumber(versionNumber)
    }

    @CompileStatic
    public List<IParameterObjectClassifier> configureClassifier(String path, List<IParameterObjectClassifier> classifiers) {
        return classifiers
    }

    List<String> getSortedProperties() {
        def names = getClass().metaClass.properties.name - ['class', 'metaClass']
        List<String> sortedProps = []
        orderNamesByDeclaredFields(getClass(), names, sortedProps)
        return sortedProps
    }

    private orderNamesByDeclaredFields(Class clazz, List<String> fieldNames, List sortedProp) {
        if (clazz.superclass) {
            orderNamesByDeclaredFields(clazz.superclass, fieldNames, sortedProp)
        }
        clazz.declaredFields.each { Field field ->
            if (field.name in fieldNames) {
                sortedProp << field.name
            }
        }
    }

    @CompileStatic
    Closure createResultNavigatorMapping() {
        return null
    }

    /**
     * This method is required as the period counter provides only the projection labels and the inception date of
     * reserves may be before the projection start.
     * @return period labels before the projection start
     */
    @CompileStatic
    Set<String> periodLabelsBeforeProjectionStart() {
        []
    }

}
