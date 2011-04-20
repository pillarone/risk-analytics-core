package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.model.MigratableModel
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

public class ModelMigrator {

    private static Log LOG = LogFactory.getLog(ModelMigrator)

    Class<? extends MigratableModel> modelClass
    VersionNumber toVersion

    protected ClassLoader currentModelClassLoader

    public ModelMigrator(Class<? extends MigratableModel> modelClass) {
        this.modelClass = modelClass
        toVersion = Model.getModelVersion(modelClass);
    }

    public void migrateParameterizations() {

        LOG.info "Starting migration of ${modelClass.simpleName} to version ${toVersion}"

        for (ParameterizationDAO dao in ParameterizationDAO.findAllByModelClassName(modelClass.name)) {
            MigratableModel instance = modelClass.newInstance()

            Parameterization parameterization = new Parameterization(dao.name)
            parameterization.versionNumber = new VersionNumber(dao.itemVersion)
            parameterization.modelClass = modelClass
            parameterization.load(false)

            if (parameterization.modelVersionNumber == toVersion) {
                LOG.info "${parameterization.name} v${parameterization.versionNumber} (current model version: ${parameterization.modelVersionNumber}) already up to date - skipping migration"
                continue
            }

            LOG.info "Migrating ${parameterization.name} v${parameterization.versionNumber} (current model version: ${parameterization.modelVersionNumber})"

            for (AbstractMigration migration in instance.getMigrationChain(parameterization.modelVersionNumber, toVersion)) {
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

                LOG.info "Migrated ${parameterization.name} v${parameterization.versionNumber} to ${migration.to}"

            }

            LOG.info "Migration of ${parameterization.name} v${parameterization.versionNumber} completed."
        }
    }

    protected Model createModel(Parameterization parameterization, int periodIndex, ClassLoader loader) {
        Model model = null
        doWithContextClassLoader loader, {
            model = (Model) Class.forName(modelClass.getName(), true, loader).newInstance()
            model.init()
            model.injectComponentNames()
            parameterization.load()

            ParameterApplicator applicator = new ModelMigrationParameterApplicator(model: model, parameterization: parameterization)
            applicator.init()
            applicator.applyParameterForPeriod(periodIndex)

            parameterization.unload()
        }

        return model
    }

    public static void doWithContextClassLoader(ClassLoader cl, Closure closure) {
        Thread currentThread = Thread.currentThread()
        ClassLoader current = currentThread.contextClassLoader
        currentThread.contextClassLoader = cl
        try {
            closure.call()
        } finally {
            currentThread.contextClassLoader = current
        }
    }
}
