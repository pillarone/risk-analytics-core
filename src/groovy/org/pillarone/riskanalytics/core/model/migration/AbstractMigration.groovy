package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.model.Model
import org.apache.commons.lang.StringUtils


abstract class AbstractMigration {

    final VersionNumber from
    final VersionNumber to
    final Class modelClass


    AbstractMigration(VersionNumber from, VersionNumber to, Class modelClass) {
        this.from = from
        this.to = to
        this.modelClass = modelClass
    }

    abstract void migrateParameterization(Model source, Model target)

    URL getOldModelJarURL() {
        String modelClassName = modelClass.simpleName
        String modelPackageName = StringUtils.uncapitalize(modelClassName.substring(0, modelClassName.length() - 5))
        String url = "/models/${modelPackageName}/${modelClassName}-v${from.toString()}.jar"
        URL resource = getClass().getResource(url)
        if (resource == null) {
            throw new IllegalStateException("Migration jar not found at $url")
        }

        return resource
    }
}
