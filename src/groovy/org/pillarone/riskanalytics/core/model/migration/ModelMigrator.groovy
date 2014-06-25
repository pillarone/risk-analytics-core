package org.pillarone.riskanalytics.core.model.migration

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.ModelDAO
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
import java.beans.Introspector

public class ModelMigrator {

    private static Log LOG = LogFactory.getLog(ModelMigrator)

    Class<? extends MigratableModel> modelClass
    VersionNumber toVersion

    private static ThreadLocal<Boolean> classLoaderBeingUsed = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false
        }
    }

//    @CompileStatic Fails to compile the countBy... call with static compilation. Tested locally works fine!
    public ModelMigrator(Class<? extends MigratableModel> modelClass) {
        this.modelClass = modelClass
        toVersion = Model.getModelVersion(modelClass);
        if(ModelDAO.countByModelClassNameAndItemVersion(modelClass.name, toVersion.toString()) == 0) {
            throw new IllegalArgumentException("ModelDAO not found for ${modelClass.simpleName} ${toVersion.toString()}. skipImport enabled?")
        }
    }

    @CompileStatic
    public static boolean migrationClassLoaderBeingUsedInThisThread() {
        return classLoaderBeingUsed.get()
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

            try {
                for (AbstractMigration migration in instance.getMigrationChain(parameterization.modelVersionNumber, toVersion)) {
                    List<ParameterHolder> newParameters = []
                    for (int periodIndex = 0; periodIndex < parameterization.periodCount; periodIndex++) {
                        ClassLoader currentModelClassLoader = new ModelMigrationClassLoader([migration.oldModelJarURL] as URL[], Thread.currentThread().getContextClassLoader())
                        Model oldModel = createModel(parameterization, periodIndex, currentModelClassLoader)
                        Model newModel = createModel(parameterization, periodIndex, Thread.currentThread().contextClassLoader)

                        migration.migrateParameterization(oldModel, newModel)
                        newParameters.addAll(ParameterizationHelper.extractParameterHoldersFromModel(newModel, periodIndex))

                        try {
                            currentModelClassLoader.close()
                            LogFactory.releaseAll() //ART-850
                            Introspector.flushCaches()
                        } catch (Exception e) {
                            LOG.warn("Failed to release class loader resources - possible memory leak: ${e.message}")
                        }
                    }

                    parameterization.load()

                    parameterization.allParameterHolders.each {parameterization.removeParameter(it)}
                    newParameters.each { parameterization.addParameter(it) }
                    parameterization.modelVersionNumber = migration.to
                    parameterization.save()

                    LOG.info "Migrated ${parameterization.name} v${parameterization.versionNumber} to ${migration.to}"

                }
                LOG.info "Migration of ${parameterization.name} v${parameterization.versionNumber} completed."
            } catch (Exception e) {
                LOG.error "Migration of ${parameterization.name} v${parameterization.versionNumber} failed.", e
            }

        }

    }

    @CompileStatic
    protected Model createModel(Parameterization parameterization, int periodIndex, ClassLoader loader) {
        Model model = null
        doWithContextClassLoader loader, {
            model = (Model) Class.forName(modelClass.getName(), true, loader).newInstance()
            model.init()
            model.injectComponentNames()
            parameterization.load()

            ParameterApplicator applicator = new ModelMigrationParameterApplicator(model, parameterization)
            applicator.init()
            applicator.applyParameterForPeriod(periodIndex)

            parameterization.unload()
        }

        return model
    }

    @CompileStatic
    public static void doWithContextClassLoader(ClassLoader cl, Closure closure) {
        Thread currentThread = Thread.currentThread()
        ClassLoader current = currentThread.contextClassLoader
        classLoaderBeingUsed.set(true)
        currentThread.contextClassLoader = cl
        try {
            closure.call()
        } finally {
            currentThread.contextClassLoader = current
            classLoaderBeingUsed.set(false)
        }
    }
}
