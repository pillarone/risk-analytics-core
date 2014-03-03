package org.pillarone.riskanalytics.core.search

import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.user.UserManagement


class OwnerFilter implements ISearchFilter {

    boolean active = false

    @Override
    boolean accept(CacheItem item) {
        return active ? item.creator?.id == UserManagement.currentUser.id : true
    }
}
