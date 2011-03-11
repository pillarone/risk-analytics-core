package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier


abstract class MigrationSupport extends AbstractMigration {


    private Model currentSource
    private Model currentTarget

    MigrationSupport(VersionNumber from, VersionNumber to, Class modelClass) {
        super(from, to, modelClass)
    }

    @Override
    final void migrateParameterization(Model source, Model target) {
        currentSource = source
        currentTarget = target
        doMigrateParameterization(source, target)
        currentSource = null
        currentTarget = null

    }

    abstract void doMigrateParameterization(Model source, Model target)

    /**
     * Converts a classifier loaded by the model migration classloader (e.g. of the old model) to the same classifier, but loaded by the current classloader (e.g. the new model).
     * This should help the modeller when writing migrations involving classifiers.
     *
     * @param classifier a Parameter object classifier of the old model (e.g. loaded from the migration jar file)
     * @return the same classifier (same class, same type) loaded from the current model
     */
    protected IParameterObjectClassifier getNewClassifier(AbstractParameterObjectClassifier classifier) {
        Class classifierClass = getNewModelClassLoader().loadClass(classifier.class.name)
        return classifierClass."${classifier.typeName}"

    }


    protected ClassLoader getOldModelClassLoader() {
        currentSource.getClass().getClassLoader()
    }

    protected ClassLoader getNewModelClassLoader() {
        currentTarget.getClass().getClassLoader()
    }

}
