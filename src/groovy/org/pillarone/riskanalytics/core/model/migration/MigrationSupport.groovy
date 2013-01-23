package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.IMultiDimensionalConstraints
import org.pillarone.riskanalytics.core.model.ModelPathComponent


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

    protected void removeColumnFromConstraint(Class constraintClass, int index) {
        for (ConstrainedMultiDimensionalParameter mdp in findMultiDimensionalParametersByConstraints(currentTarget, constraintClass.newInstance())) {
            if (!mdp.isEmpty()) {
                mdp.removeColumnAt(index)
            }
        }
    }

    protected List<ConstrainedMultiDimensionalParameter> findMultiDimensionalParametersByConstraints(Model model, IMultiDimensionalConstraints constraints) {
        ConstrainedMultiDimensionalParameterCollector collector = new ConstrainedMultiDimensionalParameterCollector(constraints)
        model.accept(collector)
        return collector.result
    }

    protected void renameClassifier(Class classifier, String oldName, String newName) {
        ClassifierCollector classifierCollector = new ClassifierCollector(classifier, oldName)
        currentSource.accept(classifierCollector)

        for (ClassifierCollector.ResultPair result in classifierCollector.result) {
            def currentComponent = currentTarget
            ModelPathComponent lastComponent = result.path.pathComponents[0]
            for (int i = 0; i < result.path.pathComponents.size() - 1; i++) {
                currentComponent = currentComponent.getProperty(lastComponent.name)
                lastComponent = result.path.pathComponents[i + 1]
            }

            AbstractParameterObjectClassifier newClassifier = getNewModelClassLoader().loadClass(classifier.name)."$newName"
            currentComponent[lastComponent.name] = newClassifier.getParameterObject(result.classifierParameters)
        }
    }

    protected ClassLoader getOldModelClassLoader() {
        currentSource.getClass().getClassLoader()
    }

    protected ClassLoader getNewModelClassLoader() {
        currentTarget.getClass().getClassLoader()
    }

}
