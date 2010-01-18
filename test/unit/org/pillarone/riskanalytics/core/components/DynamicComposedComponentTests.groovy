package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.example.component.TestComponent
import org.pillarone.riskanalytics.core.example.component.TestDynamicComposedComponent

class DynamicComposedComponentTests extends GroovyTestCase {

    void testAddComponent() {
        TestDynamicComposedComponent component = new TestDynamicComposedComponent()
        shouldFail {
            component.addSubComponent null
        }
        shouldFail {
            component.addSubComponent new TestComponent()
        }
        def comp = new TestComponent()
        comp.name = "subComp"
        component.addSubComponent(comp)


    }

    void testGetProperties() {
        TestDynamicComposedComponent composedComponent = new TestDynamicComposedComponent()
        composedComponent.addSubComponent(new TestComponent(name: 'subSomeComponent'))
        composedComponent.addSubComponent(new TestComponent(name: 'subSomeOtherComponent'))
        composedComponent.addSubComponent(new TestComponent(name: 'subTestComponent'))
        assertTrue composedComponent.properties.keySet().contains("subSomeComponent")
        assertTrue composedComponent.properties.keySet().contains("subSomeOtherComponent")
        assertTrue composedComponent.properties.keySet().contains("subTestComponent")

        assertTrue composedComponent.properties.keySet().contains("inValue")
        assertTrue composedComponent.properties.keySet().contains("outValue")
    }

    void testSubComponentPropertyAccess() {
        TestDynamicComposedComponent composedComponent = new TestDynamicComposedComponent()
        TestComponent component1 = new TestComponent(name: 'subSomeComponent')
        composedComponent.addSubComponent(component1)
        TestComponent component2 = new TestComponent(name: 'subSomeOtherComponent')
        composedComponent.addSubComponent(component2)
        TestComponent component3 = new TestComponent(name: 'subTestComponent')
        composedComponent.addSubComponent(component3)

        assertSame component1, composedComponent.subSomeComponent
        assertSame component2, composedComponent.subSomeOtherComponent
        assertSame component3, composedComponent.subTestComponent

        shouldFail(MissingPropertyException, {composedComponent.subSomething})
        shouldFail(MissingPropertyException, {composedComponent.subSomething = component1})
    }
}

