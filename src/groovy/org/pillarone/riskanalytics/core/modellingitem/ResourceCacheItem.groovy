package org.pillarone.riskanalytics.core.modellingitem

import com.google.common.collect.ImmutableList
import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.Resource
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.workflow.Status

@CompileStatic
class ResourceCacheItem extends CacheItem {

    final VersionNumber versionNumber
    final ImmutableList<Tag> tags
    final boolean valid
    final Status status

    ResourceCacheItem(
            Long id,
            String name,
            Class modelClass,
            VersionNumber versionNumber,
            DateTime creationDate,
            DateTime modificationDate,
            Person creator,
            Person lastUpdater,
            ImmutableList<Tag> tags,
            boolean valid,
            Status status
    ) {
        super(id, creationDate, modificationDate, creator, lastUpdater, name, modelClass)
        this.versionNumber = versionNumber
        this.tags = tags
        this.valid = valid
        this.status = status
    }

    ResourceCacheItem(Long id, String name, Class modelClass, VersionNumber versionNumber) {
        this(id, name, modelClass, versionNumber, null, null, null, null, null, false, null)
    }

    @Override
    String getNameAndVersion() {
        "$name v${versionNumber?.toString()}"
    }

    @Override
    Class getItemClass() {
        Resource
    }
}
