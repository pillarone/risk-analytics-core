package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.example.model.TestModel
import org.pillarone.riskanalytics.core.components.Component

class ModelTests extends GroovyTestCase {

    Model model = new TestModel()

    void testAllComponents() {
        model.initComponents()
        model.initAllComponents()
        assertNotNull model.allComponents
        assertFalse model.allComponents.empty
    }

    void testStartComponents() {
        model.initComponents()
        model.initAllComponents()
        assertNotNull model.startComponents
        assertFalse model.startComponents.empty
        assertTrue model.startComponents.size() < model.allComponents.size()
    }

    void testInjectionOfComponentNames() {
        model.initComponents()
        model.frequencyGenerator.name = "myGenerator"
        model.injectComponentNames()
        assertEquals "name not to be overwritten", "myGenerator", model.frequencyGenerator.name
        assertEquals "property name is injected as default", "claimsGenerator", model.claimsGenerator.name
    }

    void testInitComponents() {
        List allComponents = model.properties.values().grep(Component)
        assertTrue("Components have to be intialized in initComponents", allComponents.empty)
        model.initComponents()
        model.initAllComponents()
        allComponents = model.properties.values().grep(Component)
        assertFalse("No component has been initialized", allComponents.empty)
        allComponents.each { assertNotNull it }
        assertEquals(model.properties.values().grep(Component).size(), model.allComponents.size())
    }

    void testWireComponents() {
        model.initComponents()
        assertEquals(0, model.frequencyGenerator.allOutputTransmitter.size())
        model.wire()
        assertEquals(1, model.frequencyGenerator.allOutputTransmitter.size())
    }

}

