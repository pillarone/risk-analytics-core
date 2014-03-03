package org.pillarone.riskanalytics.core.modellingitem

import com.google.common.collect.ImmutableList
import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.workflow.Status

@CompileStatic
final class ParameterizationCacheItem extends CacheItem {

    final VersionNumber versionNumber
    final ImmutableList<Tag> tags
    final boolean valid
    final Status status
    final Long dealId

    ParameterizationCacheItem(
            Long id, VersionNumber versionNumber, String name, Class modelClass, DateTime creationDate, DateTime modificationDate, Person creator, Person lastUpdater, ImmutableList<Tag> tags, boolean valid, Status status, Long dealId
    ) {
        super(id, creationDate, modificationDate, creator, lastUpdater, name, modelClass)
        this.versionNumber = versionNumber

        this.tags = tags
        this.valid = valid
        this.status = status
        this.dealId = dealId
    }

    ParameterizationCacheItem(Long id, String name, Class modelClass, VersionNumber versionNumber) {
        this(id, versionNumber, name, modelClass, null, null, null, null, null, true, null, null)
    }

    @Override
    Class getItemClass() {
        Parameterization
    }
}
