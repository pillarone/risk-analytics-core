package org.pillarone.riskanalytics.core.model

import groovy.transform.CompileStatic
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.components.ResourceRegistry
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import models.core.CoreModel
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.components.IResource
import models.resource.ResourceModel
import org.pillarone.riskanalytics.core.simulation.item.Resource
import org.pillarone.riskanalytics.core.example.component.ExampleResource
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

import static org.junit.Assert.assertEquals

class ModelVisitorTests {

    Model model

    @Before
    void setUp() {
        ResourceRegistry.clear()
        FileImportService.importModelsIfNeeded(['Core'])
        Parameterization parameterization = new Parameterization("CoreParameters")
        parameterization.load()

        model = new CoreModel()
        model.init()
        model.injectComponentNames()

        ParameterApplicator applicator = new ParameterApplicator(parameterization: parameterization, model: model)
        applicator.init()
        applicator.applyParameterForPeriod(0)
    }

    @Test
    void testAccept() {
        TestModelVisitor visitor = new TestModelVisitor()
        model.accept(visitor)


        assertEquals 1, visitor.calledForModel.size()
        assertEquals 6, visitor.calledForComponent.size()
        assertEquals 4, visitor.calledForParameterObject.size()

    }

    @Test
    void testAcceptResource() {

        Resource resource = new Resource("myResource", ExampleResource)
        resource.addParameter(ParameterHolderFactory.getHolder("parmString", 0, "param"))
        resource.addParameter(ParameterHolderFactory.getHolder("parmInteger", 0, 100))
        resource.save()

        FileImportService.importModelsIfNeeded(['Resource'])
        Parameterization parameterization = new Parameterization("ResourceParameters")
        parameterization.load()

        model = new ResourceModel()
        model.init()
        model.injectComponentNames()

        ParameterApplicator applicator = new ParameterApplicator(parameterization: parameterization, model: model)
        applicator.init()
        applicator.applyParameterForPeriod(0)

        TestModelVisitor visitor = new TestModelVisitor()
        model.accept(visitor)

        assertEquals(1, visitor.calledForResource.size())
    }

}

@CompileStatic
class TestModelVisitor implements IModelVisitor {

    List<Component> calledForComponent = []
    List<Model> calledForModel = []
    List<IParameterObject> calledForParameterObject = []
    List<IResource> calledForResource = []

    void visitComponent(Component component, ModelPath path) {
        calledForComponent << component
    }

    void visitModel(Model model) {
        calledForModel << model
    }

    void visitParameterObject(IParameterObject parameterObject, ModelPath path) {
        calledForParameterObject << parameterObject
    }

    void visitResource(IResource resource, ModelPath path) {
        calledForResource << resource
    }
}
