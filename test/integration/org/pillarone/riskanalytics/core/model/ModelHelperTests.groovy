package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.fileimport.ParameterizationImportService
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import models.core.CoreModel
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.fileimport.ModelStructureImportService


class ModelHelperTests extends GroovyTestCase {

    void testOutPaths() {
        new ParameterizationImportService().compareFilesAndWriteToDB(['Core'])
        new ModelStructureImportService().compareFilesAndWriteToDB(['Core'])

        Parameterization parameterization = new Parameterization("CoreParameters")
        parameterization.load(true)

        CoreModel coreModel = new CoreModel()
        coreModel.init()

        ParameterApplicator applicator = new ParameterApplicator(model: coreModel, parameterization: parameterization)
        applicator.init()
        applicator.applyParameterForPeriod(0)

        Set paths = ModelHelper.getAllPossibleOutputPaths(coreModel, null)
        assertEquals 6, paths.size()
        assertTrue paths.any {it.contains("hierarchyLevel")}

        assertEquals 0, ModelHelper.getAllPossibleFields(coreModel, false).size()
    }
}
