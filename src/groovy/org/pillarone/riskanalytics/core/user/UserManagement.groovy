package org.pillarone.riskanalytics.core.user

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

class UserManagement {

    public static final String USER_ROLE = "ROLE_USER"
    public static final String REVIEWER_ROLE = "ROLE_REVIEWER"
    public static final String ADMIN_ROLE = "ROLE_ADMIN"
    static boolean _hasTriedFindingFixUserNameFromSystemProperty = false
    static Person _possiblyFixUser


    public static Person getCurrentUser() {
        SpringSecurityService securityService = getSpringSecurityService()
        Person user = (Person) securityService.getCurrentUser()
        return user ?: tryFindingFixUserNameFromSystemProperty()
    }

    private static Person tryFindingFixUserNameFromSystemProperty() {
        if (_hasTriedFindingFixUserNameFromSystemProperty) {
            return _possiblyFixUser
        }
        Person.withTransaction { e ->
            String runAsUserName = System.getProperty("runAsUserName");
            _possiblyFixUser = runAsUserName != null ? Person.findByUsername(runAsUserName) : null
            if (_possiblyFixUser) {
                // loading the settings seems needed here for later retrieval...?
                _possiblyFixUser.settings
            }
            _hasTriedFindingFixUserNameFromSystemProperty = true
            return _possiblyFixUser
        }
    }

    static SpringSecurityService getSpringSecurityService() {
        Holders.grailsApplication.mainContext.getBean('springSecurityService', SpringSecurityService)
    }
}
