package org.pillarone.riskanalytics.core.parameterization

import com.google.common.collect.MapMaker
import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.model.migration.ModelMigrator

@CompileStatic
class ConstraintsFactory {

    private static Log LOG = LogFactory.getLog(ConstraintsFactory)

    private static Map<String, IMultiDimensionalConstraints> constraints = new MapMaker().makeMap()

    static void registerConstraint(IMultiDimensionalConstraints constraint) {
        String identifier = constraint.name
        IMultiDimensionalConstraints existingConstraint = constraints.get(identifier)
        if (existingConstraint == null) {
            constraints.put(identifier, constraint)
        } else {
            if (existingConstraint.getClass().name == constraint.getClass().name) {
                LOG.warn "Constraint $identifier already exists - ignoring"
            } else {
                throw new IllegalStateException("Constraint $identifier already associated with ${existingConstraint.getClass().name}")
            }
        }
    }

    static IMultiDimensionalConstraints getConstraints(String name) {
        return loadConstraint(name)
    }

    protected static IMultiDimensionalConstraints loadConstraint(String name) {
        IMultiDimensionalConstraints constraint = constraints.get(name)
        if (constraint != null) {
            if (ModelMigrator.migrationClassLoaderBeingUsedInThisThread()) { //during migration we want to load the class in the context class loader, but during a kti run the context cl is not set to the grid class loader..
                //workaround for migration
                final String name1 = constraint.class.name
                Class clazz = Thread.currentThread().contextClassLoader.loadClass(name1)
                if (clazz != constraint.class) {
                    return (IMultiDimensionalConstraints) clazz.newInstance()
                }
            }
        }
        return constraint
    }
}