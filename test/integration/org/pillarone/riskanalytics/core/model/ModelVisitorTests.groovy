package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import models.core.CoreModel
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator


class ModelVisitorTests extends GroovyTestCase {

    Model model

    void setUp() {
        FileImportService.importModelsIfNeeded(['Core'])
        Parameterization parameterization = new Parameterization("CoreParameters")
        parameterization.load()

        model = new CoreModel()
        model.init()

        ParameterApplicator applicator = new ParameterApplicator(parameterization: parameterization, model: model)
        applicator.init()
        applicator.applyParameterForPeriod(0)
    }

    void testAccept() {
        TestModelVisitor visitor = new TestModelVisitor()
        model.accept(visitor)


        assertEquals 1, visitor.calledForModel.size()
        assertEquals 6, visitor.calledForComponent.size()
        assertEquals 4, visitor.calledForParameterObject.size()

    }

    private static class TestModelVisitor implements IModelVisitor {

        List<Component> calledForComponent = []
        List<Model> calledForModel = []
        List<IParameterObject> calledForParameterObject = []

        void visitComponent(Component component) {
            calledForComponent << component
        }

        void visitModel(Model model) {
            calledForModel << model
        }

        void visitParameterObject(IParameterObject parameterObject) {
            calledForParameterObject << parameterObject
        }

    }
}
