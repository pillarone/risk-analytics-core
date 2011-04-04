package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.model.MigratableModel
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper

public class ModelMigrator {

    Class<? extends MigratableModel> modelClass
    VersionNumber toVersion

    protected ClassLoader currentModelClassLoader

    public ModelMigrator(Class<? extends MigratableModel> modelClass) {
        this.modelClass = modelClass
        toVersion = Model.getModelVersion(modelClass);
    }

    public void migrateParameterizations() {
        for (ParameterizationDAO dao in ParameterizationDAO.findAllByModelClassName(modelClass.name)) {
            MigratableModel instance = modelClass.newInstance()

            Parameterization parameterization = new Parameterization(dao.name)
            parameterization.versionNumber = new VersionNumber(dao.itemVersion)
            parameterization.modelClass = modelClass
            parameterization.load(false)


            for (AbstractMigration migration in instance.getMigrationChain(parameterization.versionNumber, toVersion)) {
                List<ParameterHolder> newParameters = []
                for (int periodIndex = 0; periodIndex < parameterization.periodCount; periodIndex++) {
                    currentModelClassLoader = new ModelMigrationClassLoader([migration.oldModelJarURL] as URL[], Thread.currentThread().getContextClassLoader())
                    Model oldModel = createModel(parameterization, periodIndex, currentModelClassLoader)
                    Model newModel = createModel(parameterization, periodIndex, Thread.currentThread().contextClassLoader)

                    migration.migrateParameterization(oldModel, newModel)
                    newParameters.addAll(ParameterizationHelper.extractParameterHoldersFromModel(newModel, periodIndex))
                }

                parameterization.load()
                parameterization.parameterHolders*.removed = true
                newParameters.each { parameterization.addParameter(it) }
                parameterization.modelVersionNumber = migration.to
                parameterization.save()
            }

        }
    }

    protected Model createModel(Parameterization parameterization, int periodIndex, ClassLoader loader) {
        Model oldModel = null
        doWithContextClassLoader loader, {
            oldModel = (Model) Class.forName(modelClass.getName(), true, loader).newInstance()
            oldModel.init()
            parameterization.load()

            ParameterApplicator applicator = new ModelMigrationParameterApplicator(model: oldModel, parameterization: parameterization)
            applicator.init()
            applicator.applyParameterForPeriod(periodIndex)

            parameterization.unload()
        }

        return oldModel
    }

    public static void doWithContextClassLoader(ClassLoader cl, Closure closure) {
        Thread currentThread = Thread.currentThread()
        ClassLoader current = currentThread.contextClassLoader
        currentThread.contextClassLoader = cl
        closure.call()
        currentThread.contextClassLoader = current
    }
}
